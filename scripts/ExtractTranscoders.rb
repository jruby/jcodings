require 'open-uri'
repo_path = ARGV.first # path to ruby repo

trans_path = "#{repo_path}/enc/trans"

dst_dir = "../src/org/jcodings"
dst_bin_dir = "../resources/tables"
trans_dir = "#{dst_dir}/transcode"
trans_dst_dir = "#{trans_dir}/specific"
INDENT = " " * 4

NOMAP =           0x01 # /* direct map */
ONEbt =           0x02 # /* one byte payload */
TWObt =           0x03 # /* two bytes payload */
THREEbt =         0x05 # /* three bytes payload */
FOURbt =          0x06 # /* four bytes payload, UTF-8 only, macros start at getBT0 */
INVALID =         0x07 # /* invalid byte sequence */
UNDEF =           0x09 # /* legal but undefined */
ZERObt =          0x0A # /* zero bytes of payload, i.e. remove */
FUNii =           0x0B # /* function from info to info */
FUNsi =           0x0D # /* function from start to info */
FUNio =           0x0E # /* function from info to output */
FUNso =           0x0F # /* function from start to output */
STR1 =            0x11 # /* string 4 <= len <= 259 bytes: 1byte length + content */
GB4bt =           0x12 # /* GB18030 four bytes payload */
FUNsio =          0x13 # /* function from start and info to output */

WORDINDEX_SHIFT_BITS = 2

def WORDINDEX2INFO widx
    widx << WORDINDEX_SHIFT_BITS
end

def makeSTR1LEN len
    len - 4
end

def makeSTR1 bi
    (bi << 6) | STR1
end

def o1 b1
    (b1 << 8) | ONEbt
end

def o2 b1, b2
    (b1 << 8) | (b2 << 16) | TWObt
end

def o3 b1, b2, b3
     ((b1 << 8) | (b2 << 16) | (b3 << 24) | THREEbt) & 0xffffffff
end

def o4 b0, b1, b2, b3
    ((b1 << 8) | (b2 << 16) | (b3 << 24) | ((b0 & 0x07) << 5) | FOURbt) & 0xffffffff
end

def g4 b0, b1, b2, b3
    ((b0 << 8) | (b2 << 16) | ((b1 & 0xf) << 24) | ((b3 & 0x0f) << 28) | GB4bt) & 0xffffffff
end

def funsio diff
    (diff << 8) & FUNsio
end

def assert_eq a, b, msg = ""
    raise "unmet condition: #{a.inspect} == #{b.inspect}, info #{msg}" unless a == b
end

def assert_not_eq a, b, msg = ""
    raise "unmet condition: #{a.inspect} != #{b.inspect}, info: #{msg}" unless a != b
end


def assert
    raise "unmet condition" unless yield
end

t = Time.now

template = open("TranscoderTemplate.java").read

transcoder_list = []
generic_list = []

transcoder_srcs = Dir["#{trans_path}/*.c"].reject{|f| f =~ /transdb/}

# newline.c generates into ruby root, for whatever reason
transcoder_srcs << "#{repo_path}/newline.c"

transcoder_srcs.each do |f|
    src = open(f).read
    defs = Hash[src.scan(/#define\s+(.*?)\s+(.*)/)]
    src = src.gsub(/#define\s+(.*?)\s+(.*)/, "")

    name = f[/(\w+)\.c/, 1].split('_').map{|e| e.capitalize}.join("")

    src =~ /\w+?_byte_array\[(\d+?)\]\s+=\s+\{(.*?)\}\;/m
    byte_array_size = $1.to_i
    byte_array = $2

    byte_array = byte_array.gsub(/\w+?\(.+?\)/){|e| eval e}

    byte_array = byte_array.gsub(/0x(\w+)/){|e| e.to_i(16).to_s}
    byte_array = byte_array.split(",").map{|e|e.strip}
    assert_eq(byte_array.last, "") # trailing comma

    byte_array.pop
    assert_eq(byte_array.size, byte_array_size)

    assert_eq(byte_array.all?{|b| b =~ /\d+/}, true)
    byte_array = byte_array.map(&:to_i)
    assert_eq(byte_array.all?{|b| b >= 0 && b <= 255}, true)
    
    open("#{dst_bin_dir}/Transcoder_#{name}_ByteArray.bin", "wb") do|f|
        f << [byte_array_size].pack("N")
        byte_array.each{|b| f << b.chr}
    end

    src =~ /\w+?_word_array\[(\d+?)\]\s+=\s+\{(.*?)\}\;/m
    word_array_size = $1.to_i
    word_array = $2

    ["INVALID", "UNDEF", "NOMAP", "FUNso", "FUNsi"].each{|c|defs[c] = Object.const_get(c)}

    word_array = word_array.gsub(/\w+?\(.+?\)/){|e| eval e}
    
    word_array = word_array.split(',').map{|e|e.strip}
    assert_eq(word_array.last, "") # trailing comma

    word_array.pop
    assert_eq(word_array.size, word_array_size)

    word_array = word_array.map do |b|
        if b =~ /^\d+$/
            b.to_i
        else
            v = defs[b]
            assert_not_eq(v, nil, b)

            v = case v
                when Fixnum
                    v
                when /(\w+?\(.+?\))/
                    v = eval(v)
                    assert_eq(v.class, Fixnum)
                    v
                when String
                    assert_eq(v =~ /^\d+$/, 0)
                    v.to_i
                else
                    raise "unknown type"
            end
            defs[b] = v
            v
        end
    end

    assert_eq(word_array.all?{|e|e >= 0 && e <= 4294967295}, true)

    open("#{dst_bin_dir}/Transcoder_#{name}_WordArray.bin", "wb") do|f|
        f << [word_array_size].pack("N")
        word_array.each{|b| f << [b].pack("N")}
    end

    src.scan(/static\s+const\s+rb_transcoder.*?(\w+)\s+=\s+\{(.+?)\};/m) do |t_name, body|
        n = t_name.split('_')
        t_name = n[1].capitalize
        t_name += '_' + n[2..-1].join('_') unless n[2..-1].empty?

        body =  body.gsub(/(\/\*.*?\*\/)/, "").split(',').map{|e|e.strip}
        src, dst, tree_start, table_info, iul, max_in, max_out, conv, state_size, state_init, state_fini, *funcs = body

        info = defs[table_info].split(',').map{|e|e.strip}[0..-2]
        b_arr, b_arr_length, w_arr, w_arr_length = info
        assert_eq(b_arr_length.to_i, byte_array_size)
        assert_eq(w_arr_length.to_i, word_array_size)

        specific = !funcs.all?{|f| f == "NULL"}

        state_size = "0" if state_size == "sizeof(struct from_utf8_mac_status)"

        # super_name = specific ? "Base_#{t_name}_Transcoder" : "Transcoder"
        puts "specific transcoder #{t_name} doesnt exist" if specific and not File.exist?("#{trans_dst_dir}/#{t_name}_Transcoder.java")

        ts = defs[tree_start]
        ts = case ts
            when Fixnum
                ts
            when /^\d+$/
                ts.to_i
            when /(\w+?\(.+?\))/
                eval(ts)
            else
                raise "error #{defs[tree_start]}"
        end

        if specific and false # initial generation
            open("#{trans_dst_dir}/#{t_name}_Transcoder.java", "wb") << template.
                gsub(/%\{name\}/, "#{t_name}_Transcoder").
                sub(/%\{super\}/, "Transcoder").
                sub(/%\{super_ctor\}/, [src, dst, ts, "\"#{name}\"", iul, max_in, max_out, "AsciiCompatibility.#{conv.split('_').last.upcase}", state_size].join(', '))
        end

        generic_list << [src, dst, ts, "\"#{name}\"", iul, max_in, max_out, "AsciiCompatibility.#{conv.split('_').last.upcase}", state_size]
        transcoder_list << [src, dst, t_name, specific]
    end
end
open("#{trans_dir}/TranscoderList.java", "wb") << open("TranscoderListTemplate.java").read.
    sub(/%\{list\}/, transcoder_list.map{|src, dst, cls, specific| "#{INDENT*2}{#{src}, #{dst}, #{specific ? '"' + cls + '"' : 'null /*' + cls + '*/'}}"}.join(",\n")).
    sub(/%\{generic\}/, generic_list.map{|g| "#{INDENT*2}new GenericTranscoderEntry(#{g.join(', ')})"}.join(",\n"))


p Time.now - t
