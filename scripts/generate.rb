# coding: utf-8

REPO_PATH = ARGV.first || '/usr/src/ruby-2.4.2' # path to ruby repo
SECTION_NAME = "rdata"
UNICODE_VERSION = "9.0.0"
SRC_DIR = "../src/org/jcodings"
DST_BIN_DIR =  "../resources/tables"
INDENT = " " * 4

def generate_data
    generate_transoder_data
    generate_coderange_data
    generate_coderange_list
    generate_fold_data
end

def process_binary obj_name
    binary = open(obj_name, "rb"){|f|f.read}
    offset = `objdump -h -j .#{SECTION_NAME} #{obj_name}`[/\.#{SECTION_NAME}.*?(\w+)\s+\S+$/, 1].to_i(16)
    `nm --no-sort --defined-only #{obj_name}`.split("\n").map{|s|s.split(/\s+/)}.each do |address, _, name|
        yield name, binary, address.to_i(16) + offset
    end
end

def generate_transoder_data
    Dir["#{REPO_PATH}/enc/trans/*.c"].reject{|f| f =~ /transdb/}.each do |trans_file|
        # next unless trans_file =~ /utf8/
        trans_file = trans_file[/(.*)\./, 1]
        src = open("#{trans_file}.c", "rb").read
        process_binary "#{trans_file}.o" do |name, binary, address|
            case name
            when /(.*)_byte_array/
                name = $1
                size = src[/(\w+?_byte_array)\[(\d+?)\]/m, 2].to_i
                open("#{DST_BIN_DIR}/" + "Transcoder_#{name.capitalize.tr('_', '')}_ByteArray.bin", "wb") do |f|
                    f << [size].pack("N")
                    f << binary[address, size]
                end
            when /(.*)_word_array/
                name = $1
                size = src[/(\w+?_word_array)\[(\d+?)\]/m, 2].to_i
                open("#{DST_BIN_DIR}/" + "Transcoder_#{name.capitalize.tr('_', '')}_WordArray.bin", "wb") do |f|
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
    out = ranges.map do |range|
        name = range =~ /Age_(\d)_(\d)/ ? "age=#{$1}.#{$2}" : range.tr('_', '').downcase
        name = cr_map.delete(range) || name

        ([name] + aliases[name].to_a).map{|n|[n, range]}
    end.flatten(1)

    open("#{SRC_DIR}/unicode/UnicodeProperties.java", "wb") do |f| f <<
        open("UnicodePropertiesTemplate.java", "rb").read.sub(/%\{extcrs\}/, out.map{|name, table| "#{INDENT * 2}" + "new CodeRangeEntry(\"#{name}\", \"CR_#{table}\")"}.join(",\n"))
    end
end

def generate_fold_data
    process_binary "#{REPO_PATH}/enc/unicode.o" do |name, binary, address|
        case name
        when /CaseFold_11_Table/

        when /CaseUnfold_(\d+)_Table/
            case $1
            when '11'
            when '12'
            when '13'
            end
        end
    end
end

generate_data
