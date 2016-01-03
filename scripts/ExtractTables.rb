require 'open-uri'
repo_path = ARGV.first # path to ruby repo

enc_path = "#{repo_path}/enc"
folds_src = open("#{enc_path}/unicode.c").read
unicode_h = open("#{enc_path}/unicode/name2ctype.h").read
unicode_src = open("#{enc_path}/unicode/name2ctype.src").read

dst_dir = "../src/org/jcodings"
dst_bin_dir = "../resources/tables"
enc_dir = "#{dst_dir}/unicode"
INDENT = " " * 4

CLASS_MAP = {
  "ASCII-8BIT" =>   "ASCII",
  "UTF-8" =>        "UTF8",
  "US-ASCII" =>     "USASCII",
  "Big5" =>         "BIG5",
  "Big5-HKSCS" =>   "Big5HKSCS",
  "Big5-UAO" =>     "Big5UAO",
  "CP949" =>        "CP949",
  "Emacs-Mule" =>   "EmacsMule",
  "EUC-JP" =>       "EUCJP",
  "EUC-KR" =>       "EUCKR",
  "EUC-TW" =>       "EUCTW",
  "GB2312" =>       "GB2312",
  "GB18030" =>      "GB18030",
  "GBK" =>          "GBK",
  "ISO-8859-1" =>   "ISO8859_1",
  "ISO-8859-2" =>   "ISO8859_2",
  "ISO-8859-3" =>   "ISO8859_3",
  "ISO-8859-4" =>   "ISO8859_4",
  "ISO-8859-5" =>   "ISO8859_5",
  "ISO-8859-6" =>   "ISO8859_6",
  "ISO-8859-7" =>   "ISO8859_7",
  "ISO-8859-8" =>   "ISO8859_8",
  "ISO-8859-9" =>   "ISO8859_9",
  "ISO-8859-10" =>  "ISO8859_10",
  "ISO-8859-11" =>  "ISO8859_11",
  "ISO-8859-13" =>  "ISO8859_13",
  "ISO-8859-14" =>  "ISO8859_14",
  "ISO-8859-15" =>  "ISO8859_15",
  "ISO-8859-16" =>  "ISO8859_16",
  "KOI8-R" =>       "KOI8R",
  "KOI8-U" =>       "KOI8U",
  "Shift_JIS" =>    "SJIS",
  "UTF-16BE" =>     "UTF16BE",
  "UTF-16LE" =>     "UTF16LE",
  "UTF-32BE" =>     "UTF32BE",
  "UTF-32LE" =>     "UTF32LE",
  "Windows-31J" =>  "Windows_31J",           # TODO: Windows-31J is actually a variant of SJIS
  "Windows-1250" => "Windows_1250",
  "Windows-1251" => "Windows_1251",
  "Windows-1252" => "Windows_1252"
}

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

unicode_h.scan(/static\s+const\s+(\w+)\s+(\w+)\[\]\s+=\s+\{(.*?)\}\;/m).each do |(type, name, tab)|
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

raise 'can\'t find encdb.h - you need to configure and build MRI' unless File.exist? "#{repo_path}/encdb.h"

defines, other = open("#{repo_path}/encdb.h").read.tr('()', '').scan(/ENC_([A-Z_]+)(.*?);/m).partition { |a, b| a =~ /DEFINE/ }

open("#{dst_dir}/EncodingList.java", "wb") { |f| f << open("EncodingListTemplate.java", "rb").read.
    sub(/%\{defines\}/, defines.map { |cmd, name| "#{INDENT*2}EncodingDB.declare(#{name}, \"#{CLASS_MAP[name[/[^"]+/]] || (raise 'class not found for encoding ' + name)}\");" }.join("\n")).
    sub(/%\{other\}/, other.map { |cmd, from, to| "#{INDENT*2}EncodingDB.#{cmd.downcase}(#{from}#{to.nil? ? "" : to});" }.join("\n")) }
