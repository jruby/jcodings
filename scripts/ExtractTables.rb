require 'open-uri'
repo_path = ARGV.first # path to ruby repo

enc_path = "#{repo_path}/enc"
folds_src = open("#{enc_path}/unicode.c").read
unicode_src = open("#{enc_path}/unicode/name2ctype.src").read

dst_dir = "../src/org/jcodings"
dst_bin_dir = "../resources/tables"
enc_dir = "#{dst_dir}/unicode"
INDENT = " " * 4

def assert_eq a, b, msg = ""
  raise "unmet condition: #{a.inspect} == #{b.inspect}, info #{msg}" unless a == b
end

def assert_not_eq a, b, msg = ""
  raise "unmet condition: #{a.inspect} != #{b.inspect}, info: #{msg}" unless a != b
end

def extract_to to, file
  to = to.map do |t|
    case t
      when /^0x[0-9a-f]+$/
        t.to_i(16)
      else
        t = t.split(',').map { |t| t.strip }
        assert_eq(t.all? { |e| e =~ /^0x[0-9a-f]+$/ }, true)
        t.map { |t| t.to_i(16) }
    end
  end

  open(file, "wb") do |f|
    f << [to.size].pack("N")
    to.each do |t|
      case t
        when Fixnum
          f << [1].pack("N")
          f << [t].pack("N")
        when Array
          f << [t.size].pack("N")
          t.each { |tx| f << [tx].pack("N") }
        else
          raise "foo"
      end
    end
  end
end

folds = folds_src.scan(/static\s+const\s+(\w+)\s+(\w+)\[\]\s+=\s+\{(.*?)\}\;/m).map do |(type, name, tab)|
  case type
    when /Case(\w+)_11_Type/
      from, to = tab.scan(/(\w+).+\{\w+?.+?\{(.+?)\s?\}/).transpose

      assert_eq(to.size, from.size)

      assert_eq(from.all? { |e| e =~ /^0x[0-9a-f]+$/ }, true)
      from = from.map { |e| e.to_i(16) }
      open("#{dst_bin_dir}/#{name}_From.bin", "wb") do |f|
        f << [from.size].pack("N")
        from.each { |fr| f << [fr].pack("N") }
      end

      extract_to to, "#{dst_bin_dir}/#{name}_To.bin"

    when "CaseUnfold_12_Type", "CaseUnfold_13_Type"
      fld = tab.scan(/\{\s?\{(.+?)\}.+\{\w+?.+?\{(.+?)\s?\}/).flatten

      extract_to fld, "#{dst_bin_dir}/#{name}.bin"
    else
      raise "error"
  end
end

unicode_src.scan(/static\s+const\s+(\w+)\s+(\w+)\[\]\s+=\s+\{(.*?)\}\;/m).each do |(type, name, tab)|
  tab = tab.split(",").map { |e| e.strip }
  assert_eq(tab.last, "")
  tab.pop

  size = tab.size
  head = tab.shift

  assert_eq(head =~ /^\d+$/, 0)
  head = head.to_i

  assert_eq(tab[1..-1].all? { |e| e =~ /^0x[0-9a-f]+$/ }, true)

  tab = tab.map { |e| e.to_i(16) }
  assert_eq(tab.all? { |e| e >= 0 && e <= 4294967295 }, true)

  open("#{dst_bin_dir}/#{name}.bin", "wb") do |f|
    f << [size].pack("N")
    f << [head].pack("N")
    tab.each { |e| f << [e].pack("N") }
  end
end


cr_map = unicode_src.scan(/#define (CR_.*?) (.*)/).inject(Hash.new { |h, k| k }) { |h, (k, v)| h[k] = v; h }

aliases = unicode_src[/%%(.*?)%%/m, 1].scan(/(.*?),\s+(\d+)/).inject(Hash.new { |h, k| h[k] = [] }) { |h, (name, num)| h[num.to_i] << name; h }.inject({}) do |h, (k, v)|
  full, *abbr = v.map { |e| e.strip }
  h[full] = abbr
  h
end

unicode_src.scan(/CodeRanges\[\]\s+=\s+\{(.*?)\}\;/m) do |e|
  names = e.first.scan(/CR_\w+/)

  cnames = names.map do |c|
    n = c[/CR_(.*)/, 1]
    px = case n
           when /Age_(\d)_(\d)/
             "age=#{$1}.#{$2}"
           else
             n.tr('_', '').downcase
         end

    ([px] + aliases[px].to_a).map { |n| "#{INDENT * 4}new CodeRangeEntry(\"#{n}\", \"#{cr_map[c]}\")" }.join(",\n")
  end

  open("#{enc_dir}/UnicodeProperties.java", "wb") do |f|
    f << open("UnicodePropertiesTemplate.java", "rb").read.
        sub(/%\{stdcrs\}/, cnames[0..14].join(",\n")).
        sub(/%\{extcrs\}/, cnames.join(",\n"))
  end
end

enc_db = open("#{repo_path}/encdb.h").read.tr('()', '').scan(/ENC_([A-Z_]+)(.*?);/m).reject { |a, b| a =~ /DEFINE/ }

open("#{dst_dir}/EncodingList.java", "wb") { |f| f << open("EncodingListTemplate.java", "rb").read.
    sub(/%\{body\}/, enc_db.map { |cmd, from, to| "#{INDENT*2}{\"#{cmd[0, 1]}\", #{from}#{to.nil? ? "" : to}}" }.join(",\n")) }
