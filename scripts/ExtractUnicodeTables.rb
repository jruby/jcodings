require 'open-uri'
enc_path = "http://svn.ruby-lang.org/repos/ruby/branches/ruby_1_9_3/enc"
folds_src = open("#{enc_path}/unicode.c").read
unicode_src = open("#{enc_path}/unicode/name2ctype.src").read
dest_dir = "../src/org/jcodings/unicode/"
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

open("#{dest_dir}/UnicodeCaseFolds.java", "wb"){|f| f << open("UnicodeCaseFoldsTemplate.java", "rb").read.sub(/%\{body\}/, folds.join)}

unicode_src.scan(/static\s+const\s+(\w+)\s+(\w+)\[\]\s+=\s+\{(.*?)\}\;/m).each do |(type, name, tab)|
    unicode = "#{INDENT}static final int Table[] = Config.USE_UNICODE_PROPERTIES ? new int[] {" + tab.gsub("\t", INDENT * 2) + "#{INDENT}} : null; \n"
    open("#{dest_dir}/#{name}.java", "wb"){|f| f << open("UnicodeTableTemplate.java", "rb").read.sub(/%\{class\}/, name).sub(/%\{body\}/, unicode)}
end

cr_map =  unicode_src.scan(/#define (CR_.*?) (.*)/).inject(Hash.new{|h, k|k}){|h, (k,v)| h[k] = v;h}

unicode_src.scan(/CodeRanges\[\]\s+=\s+\{(.*?)\}\;/m) do |e|
    names = e.first.scan(/CR_\w+/)
    crs = names.map{|c|cr_map[c]}.map{|c|"#{INDENT * 4}#{c}.Table"}
    cnames = names.map{|n| "#{INDENT * 4}\"#{n[/CR_(.*)/, 1]}\".getBytes()"}
    open("#{dest_dir}/UnicodeProperties.java", "wb"){|f| f << open("UnicodePropertiesTemplate.java", "rb").read.
        sub(/%\{stdcrs\}/, crs[0..14].join(",\n")).
        sub(/%\{extcrs\}/, crs.join(",\n")).
        sub(/%\{stdnames\}/, cnames[0..14].join(",\n")).
        sub(/%\{extnames\}/, cnames.join(",\n"))}
end
