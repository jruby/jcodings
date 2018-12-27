#!/usr/bin/env ruby
# coding: utf-8

REPO_PATH = ARGV.first || '/usr/src/ruby' # path to ruby repo
SECTION_NAME, G_PREFIX = case RUBY_PLATFORM
when /linux/i; ["rodata", ""]
when /darwin/i; ["const_data", "g"]
else ;["rdata", ""]
end

UNICODE_VERSION = "10.0.0"
SRC_DIR = "../src/org/jcodings"
DST_BIN_DIR =  "../resources/tables"
INDENT = " " * 4

def generate_data
    generate_encoding_list
    generate_transcoder_list
    generate_transoder_data
    generate_coderange_data
    generate_coderange_list
    generate_fold_data
end

def process_binary obj_name
    binary = open(obj_name, "rb"){|f|f.read}
    offset = `#{G_PREFIX}objdump -h -j .#{SECTION_NAME} #{obj_name}`[/\.#{SECTION_NAME}.*?(\w+)\s+\S+$/, 1].to_i(16)
    `#{G_PREFIX}nm --no-sort --defined-only #{obj_name}`.split("\n").map{|s|s.split(/\s+/)}.each do |address, _, name|
        yield name, binary, address.to_i(16) + offset
    end
end

def generate_encoding_list

    enc_map = {
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
      "Windows-1252" => "Windows_1252",
      "Windows-1253" => "Windows_1253",
      "Windows-1254" => "Windows_1254",
      "Windows-1257" => "Windows_1257"
    }

    defines, other = open("#{REPO_PATH}/encdb.h").read.tr('()', '').scan(/ENC_([A-Z_]+)(.*?);/m).partition { |a, b| a =~ /DEFINE/ }
    other << ["ALIAS", "\"MS932\", \"Windows-31J\""]

    open("#{SRC_DIR}/EncodingList.java", "wb") { |f| f << open("EncodingListTemplate.java", "rb").read.
        sub(/%\{defines\}/, defines.map { |cmd, name| "#{INDENT*2}EncodingDB.declare(#{name}, \"#{enc_map[name[/[^"]+/]] || (raise 'class not found for encoding ' + name)}\");" }.join("\n")).
        sub(/%\{other\}/, other.map { |cmd, from, to| "#{INDENT*2}EncodingDB.#{cmd.downcase}(#{from}#{to.nil? ? "" : to});" }.join("\n")).
        sub(/%\{switch\}/, defines.map { |cmd, name| "#{INDENT*3}case \"#{enc_map[name[/[^"]+/]]}\": return #{enc_map[name[/[^"]+/]]}Encoding.INSTANCE;"}.join("\n"))}

end

def generate_transcoder_list
    generic_list = []
    transcoder_list = []

    Dir["#{REPO_PATH}/enc/trans/*.c"].reject{|f| f =~ /transdb/}.sort.each do |trans_file|
        name = trans_file[/(\w+)\.c/, 1].split('_').map{|e| e.capitalize}.join("")
        trans_src = open(trans_file){|f|f.read}

        trans_src.scan(/static\s+const\s+rb_transcoder.*?(\w+)\s+=\s+\{(.+?)\};/m) do |t_name, body|
            n = t_name.split('_')
            t_name = n[1].capitalize
            t_name += '_' + n[2..-1].join('_') unless n[2..-1].empty?
            body = body.gsub(/(\/\*.*?\*\/)/, "").split(',').map{|e|e.strip}
            src, dst, tree_start, table_info, iul, max_in, max_out, conv, state_size, state_init, state_fini, *funcs = body
            tree_start = trans_src[/#define\s+#{tree_start}\s+WORDINDEX2INFO\((\d+)\)/, 1].to_i << 2
            state_size = "0" if state_size == "sizeof(struct from_utf8_mac_status)"
            generic = funcs.all?{|f|f == "NULL"}

            generic_list << [src, dst, tree_start, "\"#{name}\"", iul, max_in, max_out, "AsciiCompatibility.#{conv.split('_').last.upcase}", state_size] if generic
            transcoder_list << [src, dst, t_name, !generic]
        end

    end
    open("#{SRC_DIR}/transcode/TranscoderList.java", "wb") << open("TranscoderListTemplate.java", "rb"){|f|f.read}.
        sub(/%\{list\}/, transcoder_list.map{|src, dst, cls, specific| "#{INDENT*2}TranscoderDB.declare(#{src}, #{dst}, #{specific ? '"' + cls + '"' : 'null /*' + cls + '*/'});"}.join("\n")).
        sub(/%\{generic\}/, generic_list.map{|g| "#{INDENT*2}new GenericTranscoderEntry(#{g.join(', ')})"}.join(",\n")).
        sub(/%\{switch\}/, transcoder_list.map{|src, dst, cls, specific| "#{INDENT*3}case \"#{cls}\": return #{cls}_Transcoder.INSTANCE;" if specific}.compact.join("\n"))

end

def generate_transoder_data
    Dir["#{REPO_PATH}/enc/trans/*.c"].reject{|f| f =~ /transdb/}.sort.each do |trans_file|
        # next unless trans_file =~ /utf8/
        trans_file = trans_file[/(.*)\./, 1]
        src = open("#{trans_file}.c", "rb").read
        make_name = -> (name) {name.split('_').map{|e|e.capitalize}.join('')}
        process_binary "#{trans_file}.o" do |name, binary, address|
            case name
            when /(.*)_byte_array/
                name = $1
                size = src[/(\w+?_byte_array)\[(\d+?)\]/m, 2].to_i
                open("#{DST_BIN_DIR}/" + "Transcoder_#{make_name.(name)}_ByteArray.bin", "wb") do |f|
                    f << [size].pack("N")
                    f << binary[address, size]
                end
            when /(.*)_word_array/
                name = $1
                size = src[/(\w+?_word_array)\[(\d+?)\]/m, 2].to_i
                open("#{DST_BIN_DIR}/" + "Transcoder_#{make_name.(name)}_WordArray.bin", "wb") do |f|
                    f << [size].pack("N")
                    address.step(address + (size * 4 - 1), 4).each do |adr|
                        f << binary[adr, 4].unpack("l").pack("N")
                    end
                end
            end
        end
    end
end

def generate_coderange_data
    process_binary "#{REPO_PATH}/enc/unicode.o" do |name, binary, address|
        case name
        when /CR_(.*)/
            size = binary[address, 4].unpack("l")
            address += 4
            open("#{DST_BIN_DIR}/#{name}.bin", "wb") do |f|
                f << [size[0] * 2 + 1].pack("N")
                f << size.pack("N")

                address.step(address + (size[0] * 2 * 4 - 1), 4).each do |adr|
                    f << binary[adr, 4].unpack("l").pack("N")
                end
            end
        end
    end
end

def generate_coderange_list
    name2ctype_h = "#{REPO_PATH}/enc/unicode/#{UNICODE_VERSION}/name2ctype.h"
    cr_map = open("#{name2ctype_h}", "rb"){|f|f.read}.scan(/#define CR_(.*?) CR_(.*)/).inject({}){|h, (k, v)|h[v] = k.tr('_', '').downcase; h}
    unicode_src = `cpp #{name2ctype_h} -DUSE_UNICODE_PROPERTIES -DUSE_UNICODE_AGE_PROPERTIES | grep "^[^#;]"`
    gperf_map = Hash[unicode_src[/struct\s+uniname2ctype_pool_t\s+\{(.*?)\}\;/m, 1].scan(/uniname2ctype_pool_str(\d+).*\"(\S+)\"/)]

    aliases = unicode_src[/wordlist\[\]\s+=\s+\{(.*?)\}\;/m, 1].scan(/uniname2ctype_pool_str(\d+).*?(\d+)/).
        inject(Hash.new{|h, k|h[k] = []}){|h, (name, num)|h[num] << gperf_map[name]; h}.inject({}) do |h, (k, v)|
        h.merge! Hash[v.map{|val|[val, v - [val]]}]
        h
    end

    ranges = unicode_src[/CodeRanges\[\]\s+=\s+\{(.*?)\}\;/m, 1].scan(/CR_(\w+)/).flatten

    standard_char_type_range = 16
    out = ranges.take(standard_char_type_range).map{|range|[range.tr('_', '').downcase, range]} +
    ranges.drop(standard_char_type_range).map do |range|
        name = range =~ /Age_(\d+)_(\d+)/ ? "age=#{$1}.#{$2}" : range.tr('_', '').downcase
        name = cr_map.delete(range) || name
        name = "#{$1}=#{$2}" if name =~ /(graphemeclusterbreak)(.*)/i
        ([name] + aliases[name].to_a).map{|n|[n, range]}
    end.flatten(1)
    max_length = out.max_by{|name, table|name.length}.first.length.to_s

    open("#{SRC_DIR}/unicode/UnicodeProperties.java", "wb") do |f| f <<
        open("UnicodePropertiesTemplate.java", "rb").read.sub(/%\{max_length\}/, max_length).sub(/%\{extcrs\}/, out.map{|name, table| "#{INDENT * 2}" + "new CodeRangeEntry(\"#{name}\", \"CR_#{table}\")"}.join(",\n"))
    end
end

def generate_fold_data
    src = open("#{REPO_PATH}/enc/unicode/#{UNICODE_VERSION}/casefold.h"){|f|f.read}
    offsets = src.scan(/#define (Case\S+).*?\[(\w+)\].*?\+(\d+)/).inject({}){|h, (k, *v)| h[k] = v.map(&:to_i);h}

    extract = -> (f, binary, address, from, range, from_w, to_w) do
        f << [0].pack("N")

        width = from_w + to_w
        size = 0
        start = address + from * width * 4
        start.step(start + (range * width * 4 - 1), width * 4) do |adr|
            f << binary[adr, from_w * 4].unpack("l*").pack("N*")
            packed = binary[adr + from_w * 4, 4].unpack("l").first
            length = packed & 7
            size += length
            f << [packed].pack("N")
            f << binary[adr + from_w * 4 + 4, length * 4].unpack("l*").pack("N*")
        end
        f.seek(0)
        vrange = size - (size - range)
        f << [(range + vrange) / 2].pack("N")
    end

    process_binary "#{REPO_PATH}/enc/unicode.o" do |name, binary, address|
        case name
        when /(CaseFold)_11_Table/
            name = $1
            range, from = offsets[name]
            range += offsets[name + '_Locale'].first

            open("#{DST_BIN_DIR}/CaseFold.bin", "wb") do |f|
                extract.(f, binary, address, from, range, 1, 4)
            end
        when /(CaseUnfold_(\d+))_Table/
            name = $1
            case $2
            when '11'
                range, from = offsets[name]
                open("#{DST_BIN_DIR}/CaseUnfold_11.bin", "wb") do |f|
                    extract.(f, binary, address, from, range, 1, 4)
                end
                range, from = offsets[name + '_Locale']
                open("#{DST_BIN_DIR}/CaseUnfold_11_Locale.bin", "wb") do |f|
                    extract.(f, binary, address, from, range, 1, 4)
                end
            when '12'
                range, from = offsets[name]
                open("#{DST_BIN_DIR}/CaseUnfold_12.bin", "wb") do |f|
                    extract.(f, binary, address, from, range, 2, 3)
                end
                range, from = offsets[name + '_Locale']
                open("#{DST_BIN_DIR}/CaseUnfold_12_Locale.bin", "wb") do |f|
                    extract.(f, binary, address, from, range, 2, 3)
                end
            when '13'
                range, from = offsets[name]
                open("#{DST_BIN_DIR}/CaseUnfold_13.bin", "wb") do |f|
                    extract.(f, binary, address, from, range, 3, 3)
                end
            end

        when /CaseMappingSpecials/
            open("#{DST_BIN_DIR}/CaseMappingSpecials.bin", "wb") do |f|
                size =  src[/CaseMappingSpecials\[\]\s+=\s+\{(.*?)\}\;/m, 1].scan(/0x[0-9A-F]{4}/).size
                f << [size].pack("N")
                address.step(address + (size * 4 - 1), 4).each do |adr|
                    f << binary[adr, 4].unpack("l").pack("N")
                end
            end
        end
    end
end

generate_data
