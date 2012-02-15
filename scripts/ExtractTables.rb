require 'open-uri'
repo_path = ARGV.first # path to ruby repo

enc_path = "#{repo_path}/enc"
folds_src = open("#{enc_path}/unicode.c").read
unicode_src = open("#{enc_path}/unicode/name2ctype.src").read

dst_dir = "../src/org/jcodings"
enc_dir = "#{dst_dir}/unicode"
INDENT = " " * 4

folds = folds_src.scan(/static\s+const\s+(\w+)\s+(\w+)\[\]\s+=\s+\{(.*?)\}\;/m).map do |(type, name, tab)|
    case type
        when /Case(\w+)_11_Type/
            from, to = tab.scan(/(\w+).+\{\w+?.+?\{(.+?)\s?\}/).transpose
            "#{INDENT}static final int #{name}_From[] = {\n#{INDENT*2}" + from.join(",\n#{INDENT*2}") + "\n#{INDENT}}; /* #{name}_From */\n\n" +
            "#{INDENT}static final int #{name}_To[][] = new int[][] {\n" + to.map{|t| "#{INDENT*2}{#{t}}"}.join(",\n") + "\n#{INDENT}}; /* #{name}_To */\n\n"
        when "CaseUnfold_12_Type", "CaseUnfold_13_Type"
            fld =  tab.scan(/\{\s?\{(.+?)\}.+\{\w+?.+?\{(.+?)\s?\}/).map{|t| "#{INDENT*2}{#{t[0]}}, {#{t[1]}}"}.join(",\n")
            "#{INDENT}static final int #{name}[][] = new int[][] {\n" + fld + "\n#{INDENT}}; /* #{name} */\n\n"
    end
end

open("#{enc_dir}/UnicodeCaseFolds.java", "wb"){|f| f << open("UnicodeCaseFoldsTemplate.java", "rb").read.sub(/%\{body\}/, folds.join)}

unicode_src.scan(/static\s+const\s+(\w+)\s+(\w+)\[\]\s+=\s+\{(.*?)\}\;/m).each do |(type, name, tab)|
    unicode = "#{INDENT}static final int Table[] = Config.USE_UNICODE_PROPERTIES ? new int[] {" + tab.gsub("\t", INDENT * 2) + "#{INDENT}} : null; \n"
    open("#{enc_dir}/#{name}.java", "wb"){|f| f << open("UnicodeTableTemplate.java", "rb").read.sub(/%\{class\}/, name).sub(/%\{body\}/, unicode)}
end

cr_map =  unicode_src.scan(/#define (CR_.*?) (.*)/).inject(Hash.new{|h, k|k}){|h, (k,v)| h[k] = v;h}

def convert_property name
    n = name[/CR_(.*)/, 1]
    case n
        when /Age_(\d)_(\d)/
            "age=#{$1}.#{$2}"
        else
            n.tr('_', '').downcase
    end
end

aliases = unicode_src[/%%(.*?)%%/m, 1].scan(/(.*?),\s+(\d+)/).inject(Hash.new{|h, k| h[k] = []}){|h, (name, num)| h[num.to_i] << name; h}.inject({}) do |h, (k, v)|
    full, *abbr = v.map{|e|e.strip}
    h[full] = abbr
    h
end

unicode_src.scan(/CodeRanges\[\]\s+=\s+\{(.*?)\}\;/m) do |e|
    names = e.first.scan(/CR_\w+/)

    crs = names.map do |c|
        px = convert_property(c)
        ([c] * (aliases[px].to_a.length + 1)).map{|c|"#{INDENT * 4}#{cr_map[c]}.Table"}.join(",\n")
    end

    cnames = names.map do |n|
       px = convert_property(n)
       ([px] + aliases[px].to_a).map{|n| "#{INDENT * 4}\"#{n}\".getBytes()"}.join(",\n")
    end

    open("#{enc_dir}/UnicodeProperties.java", "wb"){|f| f << open("UnicodePropertiesTemplate.java", "rb").read.
        sub(/%\{stdcrs\}/, crs[0..14].join(",\n")).
        sub(/%\{extcrs\}/, crs.join(",\n")).
        sub(/%\{stdnames\}/, cnames[0..14].join(",\n")).
        sub(/%\{extnames\}/, cnames.join(",\n"))}
end

enc_db = open("#{repo_path}/encdb.h").read.scan(/ENC_([A-Z_]+)(.*?);/m).reject{|a, b| a =~ /DEFINE/}.map{|a, b| (INDENT * 2)  + a.downcase + b + ";" }.join("\n")
open("#{dst_dir}/EncodingDB.java", "wb"){|f| f << open("EncodingDBTemplate.java", "rb").read.sub(/%\{body\}/, enc_db)}
