package org.jcodings.transcode;

/**
 * Created by headius on 3/4/14.
 */
public class TranscodeFunctions {
    public static final int BE = 1;
    public static final int LE = 2;

    public static int funSoToUTF16(byte[] statep, byte[] sBytes, int sStart, int l, byte[] o, int oStart, int osize) {
        int sp = 0;
        if (statep[sp] == 0) {
            o[oStart++] = (byte)0xFE;
            o[oStart++] = (byte)0xFF;
            statep[sp] = (byte)1;
            return 2 + funSoToUTF16BE(statep, sBytes, sStart, l, o, oStart, osize);
        }
        return funSoToUTF16BE(statep, sBytes, sStart, l, o, oStart, osize);
    }

    public static int funSoToUTF16BE(byte[] statep, byte[] sBytes, int sStart, int l, byte[] o, int oStart, int osize) {
        if ((sBytes[sStart] & 0x80) == 0) {
            o[oStart] = (byte)0x00;
            o[oStart + 1] = sBytes[sStart];
            return 2;
        } else if ((sBytes[sStart] & 0xE0) == 0xC0) {
            o[oStart] = (byte)(((sBytes[sStart] & 0xFF) >> 2) & 0x07);
            o[oStart + 1] = (byte)(((sBytes[sStart] & 0x03) << 6) | (sBytes[sStart + 1] & 0x3F));
            return 2;
        } else if ((sBytes[sStart] & 0xF0) == 0xE0) {
            o[oStart] = (byte)(((sBytes[sStart] & 0xFF) << 4) | (((sBytes[sStart + 1] & 0xFF) >> 2) ^ 0x20));
            o[oStart + 1] = (byte)(((sBytes[sStart + 1] & 0xFF) << 6) | ((sBytes[sStart + 2] & 0xFF) ^ 0x80));
            return 2;
        } else {
            int w = (((sBytes[sStart] & 0x07) << 2) | (((sBytes[sStart + 1] & 0xFF) >> 4) & 0x03)) - 1;
            o[oStart] = (byte)(0xD8 | (w >> 2));
            o[oStart + 1] = (byte)((w << 6) | ((sBytes[sStart + 1] & 0x0F) << 2) | (((sBytes[sStart + 2] & 0xFF) >> 4) - 8));
            o[oStart + 2] = (byte)(0xDC | (((sBytes[sStart + 2] & 0xFF) >> 2) & 0x03));
            o[oStart + 3] = (byte)(((sBytes[sStart + 2] & 0xFF) << 6) | ((sBytes[sStart + 3] & 0xFF) & ~0x80));
            return 4;
        }
    }

    public static int funSoToUTF16LE(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        if ((s[sStart] & 0x80) == 0) {
            o[oStart + 1] = (byte)0x00;
            o[oStart] = s[sStart];
            return 2;
        } else if ((s[sStart] & 0xE0) == 0xC0) {
            o[oStart + 1] = (byte)(((s[sStart] & 0xFF) >> 2) & 0x07);
            o[oStart] = (byte)(((s[sStart] & 0x03) << 6) | (s[sStart + 1] & 0x3F));
            return 2;
        } else if ((s[sStart] & 0xF0) == 0xE0) {
            o[oStart + 1] = (byte)(((s[sStart] & 0xFF) << 4) | (((s[sStart + 1] & 0xFF) >> 2) ^ 0x20));
            o[oStart] = (byte)(((s[sStart + 1] & 0xFF) << 6) | ((s[sStart + 2] & 0xFF) ^ 0x80));
            return 2;
        } else {
            int w = (((s[sStart] & 0x07) << 2) | (((s[sStart + 1] & 0xFF) >> 4) & 0x03)) - 1;
            o[oStart + 1] = (byte)(0xD8 | (w >> 2));
            o[oStart] = (byte)((w << 6) | ((s[sStart + 1] & 0x0F) << 2) | (((s[sStart + 2] & 0xFF) >> 4) - 8));
            o[oStart + 3] = (byte)(0xDC | (((s[sStart + 2] & 0xFF) >> 2) & 0x03));
            o[oStart + 2] = (byte)(((s[sStart + 2] & 0xFF) << 6) | ((s[sStart + 3] & 0xFF) & ~0x80));
            return 4;
        }
    }

    public static int funSoToUTF32(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        int sp = 0;
        if (statep[sp] == 0) {
            o[oStart++] = 0x00;
            o[oStart++] = 0x00;
            o[oStart++] = (byte)0xFE;
            o[oStart++] = (byte)0xFF;
            statep[sp] = 1;
            return 4 + funSoToUTF32BE(statep, s, sStart, l, o, oStart, osize);
        }
        return funSoToUTF32BE(statep, s, sStart, l, o, oStart, osize);
    }

    public static int funSoToUTF32BE(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        o[oStart] = 0;
        if ((s[sStart] & 0x80) == 0) {
            o[oStart + 1] = o[oStart + 2] = 0x00;
            o[oStart + 3] = s[sStart];
        } else if ((s[sStart] & 0xE0) == 0xC0) {
            o[oStart + 1] = 0x00;
            o[oStart + 2] = (byte)(((s[sStart] & 0xFF) >> 2) & 0x07);
            o[oStart + 3] = (byte)(((s[sStart] & 0x03) << 6) | (s[sStart + 1] & 0x3F));
        } else if ((s[sStart] & 0xF0) == 0xE0) {
            o[oStart + 1] = 0x00;
            o[oStart + 2] = (byte)((s[sStart] << 4) | (((s[sStart + 1] & 0xFF) >> 2) ^ 0x20));
            o[oStart + 3] = (byte)((s[sStart + 1] << 6) | (s[sStart + 2] ^ 0x80));
        } else {
            o[oStart + 1] = (byte)(((s[sStart] & 0x07) << 2) | (((s[sStart + 1] & 0xFF) >> 4) & 0x03));
            o[oStart + 2] = (byte)(((s[sStart + 1] & 0x0F) << 4) | (((s[sStart + 2] & 0xFF) >> 2) & 0x0F));
            o[oStart + 3] = (byte)(((s[sStart + 2] & 0x03) << 6) | (s[sStart + 3] & 0x3F));
        }
        return 4;
    }

    public static int funSoToUTF32LE(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        o[oStart] = 0;
        if ((s[sStart] & 0x80) == 0) {
            o[oStart + 2] = o[1] = 0x00;
            o[oStart + 3] = s[sStart];
        } else if ((s[sStart] & 0xE0) == 0xC0) {
            o[oStart + 2] = 0x00;
            o[oStart + 1] = (byte)(((s[sStart] & 0xFF) >> 2) & 0x07);
            o[oStart] = (byte)(((s[sStart] & 0x03) << 6) | (s[sStart + 1] & 0x3F));
        } else if ((s[sStart] & 0xF0) == 0xE0) {
            o[oStart + 2] = 0x00;
            o[oStart + 1] = (byte)((s[sStart + 0] << 4) | (((s[sStart + 1] & 0xFF) >> 2) ^ 0x20));
            o[oStart] = (byte)((s[sStart + 1] << 6) | (s[sStart + 2] ^ 0x80));
        } else {
            o[oStart + 2] = (byte)(((s[sStart + 0] & 0x07) << 2) | (((s[sStart + 1] & 0xFF) >> 4) & 0x03));
            o[oStart + 1] = (byte)(((s[sStart + 1] & 0x0F) << 4) | (((s[sStart + 2] & 0xFF) >> 2) & 0x0F));
            o[oStart] = (byte)(((s[sStart + 2] & 0x03) << 6) | (s[sStart + 3] & 0x3F));
        }
        return 4;
    }

    public static int funSiFromUTF32(byte[] statep, byte[] s, int sStart, int l) {
        switch (statep[0]) {
            case 0:
                if (s[sStart] == 0 && s[sStart + 1] == 0 && s[sStart + 2] == 0xFE && s[sStart + 3] == 0xEE) {
                    statep[0] = BE;
                    return TranscodingInstruction.ZERObt;
                } else if (s[sStart] == 0xFF && s[sStart + 1] == 0xFE && s[sStart + 2] == 0 && s[sStart + 3] == 0) {
                    statep[0] = LE;
                    return TranscodingInstruction.ZERObt;
                }
            case BE:
                if (s[sStart] == 0 && ((0 < (s[sStart + 1] & 0xFF) && (s[sStart + 1] & 0xFF) <= 0x10)) ||
                        (s[sStart + 1] == 0 && ((s[sStart + 2] & 0xFF) < 0xD8 && 0xDF < (s[sStart + 2] & 0xFF)))) {
                    return TranscodingInstruction.FUNso;
                }
                break;
            case LE:
                if (s[sStart + 3] == 0 && ((0 < (s[sStart + 2] & 0xFF) && (s[sStart + 2] & 0xFF) <= 0x10) ||
                        (s[sStart + 2] == 0 && ((s[sStart + 1] + 0xFF) < 0xD8 || 0xDF < (s[sStart + 1] & 0xFF)))))
                    return TranscodingInstruction.FUNso;
                break;
        }
        return TranscodingInstruction.INVALID;
    }

    public static int funSoFromUTF32(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        switch (statep[0]) {
            case BE:
                return funSoFromUTF32BE(statep, s, sStart, l, o, oStart, osize);
            case LE:
                return funSoFromUTF32LE(statep, s, sStart, l, o, oStart, osize);
        }
        return 0;
    }

    public static int funSoFromUTF32BE(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        if (s[sStart + 1] == 0) {
            if (s[sStart + 2] == 0 && (s[sStart + 3] & 0xFF) < 0x80) {
                o[oStart] = s[sStart + 3];
                return 1;
            } else if ((s[sStart + 2] & 0xFF) < 0x08) {
                o[oStart] = (byte)(0xC0 | (s[sStart + 2] << 2) | ((s[sStart + 3] & 0xFF) >> 6));
                o[oStart + 1] = (byte)(0x80 | (s[sStart + 3] & 0x3F));
                return 2;
            } else {
                o[oStart] = (byte)(0xE0 | ((s[sStart + 2] & 0xFF) >> 4));
                o[oStart + 1] = (byte)(0x80 | ((s[sStart + 2] & 0x0F) << 2) | ((s[sStart + 3] & 0xFF) >> 6));
                o[oStart + 2] = (byte)(0x80 | (s[sStart + 3] & 0x3F));
                return 3;
            }
        } else {
            o[oStart] = (byte)(0xF0 | ((s[sStart + 1] & 0xFF) >> 2));
            o[oStart + 1] = (byte)(0x80 | ((s[sStart + 1] & 0x03) << 4) | ((s[sStart + 2] & 0xFF) >> 4));
            o[oStart + 2] = (byte)(0x80 | ((s[sStart + 2] & 0x0F) << 2) | ((s[sStart + 3] & 0xFF) >> 6));
            o[oStart + 3] = (byte)(0x80 | (s[sStart + 3] & 0x3F));
            return 4;
        }
    }

    public static int funSoFromUTF32LE(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        if (s[sStart + 2] == 0) {
            if (s[sStart + 1] == 0 && (s[sStart] & 0xFF) < 0x80) {
                o[oStart] = s[sStart];
                return 1;
            } else if ((s[sStart + 1] & 0xFF) < 0x08) {
                o[oStart] = (byte)(0xC0 | (s[sStart + 1] << 2) | ((s[sStart] & 0xFF) >> 6));
                o[oStart + 1] = (byte)(0x80 | (s[sStart] & 0x3F));
                return 2;
            } else {
                o[oStart] = (byte)(0xE0 | ((s[sStart + 1] & 0xFF) >> 4));
                o[oStart + 1] = (byte)(0x80 | ((s[sStart + 1] & 0x0F) << 2) | ((s[sStart] & 0xFF) >> 6));
                o[oStart + 2] = (byte)(0x80 | (s[sStart] & 0x3F));
                return 3;
            }
        } else {
            o[oStart] = (byte)(0xF0 | ((s[sStart + 2] & 0xFF) >> 2));
            o[oStart + 1] = (byte)(0x80 | ((s[sStart + 2] & 0x03) << 4) | ((s[sStart + 1] & 0xFF) >> 4));
            o[oStart + 2] = (byte)(0x80 | ((s[sStart + 1] & 0x0F) << 2) | ((s[sStart] & 0xFF) >> 6));
            o[oStart + 3] = (byte)(0x80 | (s[sStart] & 0x3F));
            return 4;
        }
    }

    public static final int from_UTF_16BE_D8toDB_00toFF = Transcoding.WORDINDEX2INFO(39);
    public static final int from_UTF_16LE_00toFF_D8toDB = Transcoding.WORDINDEX2INFO(5);

    public static int funSiFromUTF16(byte[] statep, byte[] s, int sStart, int l) {
        switch (statep[0]) {
            case 0:
                if (s[sStart] == 0xFE && s[sStart + 1] == 0xFF) {
                    statep[0] = BE;
                    return TranscodingInstruction.ZERObt;
                } else if (s[sStart] == 0xFF && s[sStart + 1] == 0xFE) {
                    statep[0] = LE;
                    return TranscodingInstruction.ZERObt;
                }
                break;
            case BE:
                if ((s[sStart] & 0xFF) < 0xD8 || 0xDF < (s[sStart] & 0xFF)) {
                    return TranscodingInstruction.FUNso;
                } else if ((s[sStart] & 0xFF) <= 0xDB) {
                    return from_UTF_16BE_D8toDB_00toFF;
                }
                break;
            case LE:
                if ((s[sStart + 1] & 0xFF) < 0xD8 || 0xDF < (s[sStart + 1] & 0xFF)) {
                    return TranscodingInstruction.FUNso;
                } else if ((s[sStart + 1] & 0xFF) <= 0xDB) {
                    return from_UTF_16LE_00toFF_D8toDB;
                }
                break;
        }
        return TranscodingInstruction.INVALID;
    }

    public static int funSoFromUTF16(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        switch (statep[0]) {
            case BE:
                return funSoFromUTF16BE(statep, s, sStart, l, o, oStart, osize);
            case LE:
                return funSoFromUTF16LE(statep, s, sStart, l, o, oStart, osize);
        }
        return 0;
    }

    public static int funSoFromUTF16BE(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        if (s[sStart] == 0 && (s[sStart + 1] & 0xFF) < 0x80) {
            o[oStart] = s[sStart + 1];
            return 1;
        } else if ((s[sStart] & 0xFF) < 0x08) {
            o[oStart] = (byte)(0xC0 | ((s[sStart] & 0xFF) << 2) | ((s[sStart + 1] & 0xFF) >> 6));
            o[oStart + 1] = (byte)(0x80 | (s[sStart + 1] & 0x3F));
            return 2;
        } else if ((s[sStart] & 0xF8) != 0xD8) {
            o[oStart] = (byte)(0xE0 | ((s[sStart] & 0xFF) >> 4));
            o[oStart + 1] = (byte)(0x80 | ((s[sStart] & 0x0F) << 2) | ((s[sStart + 1] & 0xFF) >> 6));
            o[oStart + 2] = (byte)(0x80 | (s[sStart + 1] & 0x3F));
            return 3;
        } else {
            long u = (((s[sStart] & 0x03) << 2) | ((s[sStart + 1] & 0xFF) >> 6)) + 1;
            o[oStart] = (byte)(0xF0 | (u >> 2));
            o[oStart + 1] = (byte)(0x80 | ((u & 0x03) << 4) | (((s[sStart + 1] & 0xFF) >> 2) & 0x0F));
            o[oStart + 2] = (byte)(0x80 | ((s[sStart + 1] & 0x03) << 4) | ((s[sStart + 2] & 0x03) << 2) | ((s[sStart + 3] & 0xFF) >> 6));
            o[oStart + 3] = (byte)(0x80 | (s[sStart + 3] & 0x3F));
            return 4;
        }
    }

    public static int funSoFromUTF16LE(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        if (s[sStart + 1] == 0 && s[sStart] < 0x80) {
            o[oStart] = s[sStart];
            return 1;
        } else if ((s[sStart + 1] & 0xFF) < 0x08) {
            o[oStart] = (byte)(0xC0 | (s[sStart + 1] << 2) | ((s[sStart] & 0xFF) >> 6));
            o[oStart + 1] = (byte)(0x80 | (s[sStart] & 0x3F));
            return 2;
        } else if ((s[sStart + 1] & 0xF8) != 0xD8) {
            o[oStart] = (byte)(0xE0 | ((s[sStart + 1] & 0xFF) >> 4));
            o[oStart + 1] = (byte)(0x80 | ((s[sStart + 1] & 0x0F) << 2) | ((s[sStart] & 0xFF) >> 6));
            o[oStart + 2] = (byte)(0x80 | (s[sStart] & 0x3F));
            return 3;
        } else {
            long u = (((s[sStart + 1] & 0x03) << 2) | ((s[sStart] & 0xFF) >> 6)) + 1;
            o[oStart] = (byte)(0xF0 | u >> 2);
            o[oStart + 1] = (byte)(0x80 | ((u & 0x03) << 4) | (((s[sStart] + 0xFF) >> 2) & 0x0F));
            o[oStart + 2] = (byte)(0x80 | ((s[sStart] & 0x03) << 4) | ((s[sStart + 3] & 0x03) << 2) | ((s[sStart + 2] & 0xFF) >> 6));
            o[oStart + 3] = (byte)(0x80 | (s[sStart + 2] & 0x3F));
            return 4;
        }
    }

    public static int funSoEucjp2Sjis(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        if (s[sStart] == 0x8e) {
            o[0] = s[sStart + 1];
            return 1;
        } else {
            int h, m, l2;
            m = s[sStart] & 1;
            h = ((s[sStart] & 0xFF) + m) >> 1;
            h += (s[sStart] & 0xFF) < 0xdf ? 0x30 : 0x70;
            l2 = (s[sStart + 1] & 0xFF) - m * 94 - 3;
            if (0x7f <= l2)
                l++;
            o[0] = (byte)h;
            o[1] = (byte)l2;
            return 2;
        }
    }

    public static int funSoSjis2Eucjp(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize) {
        if (l == 1) {
            o[oStart] = (byte)0x8E;
            o[oStart+1] = s[sStart];
            return 2;
        } else {
            int h, l2;
            h = s[sStart] & 0xFF;
            l2 = s[sStart + 1] & 0xFF;
            if (0xe0 <= h)
                h -= 64;
            l2 += l2 < 0x80 ? 0x61 : 0x60;
            h = h * 2 - 0x61;
            if (0xfe < l2) {
                l2 -= 94;
                h += 1;
            }
            o[oStart] = (byte)h;
            o[oStart+1] = (byte)l2;
            return 2;
        }
    }

    public static int funSoFromGB18030(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize)
    {
        long u = ((s[sStart] & 0xFF)-0x90)*10*126*10 + ((s[sStart+1] & 0xFF)-0x30)*126*10 + ((s[sStart+2]&0xFF)-0x81)*10 + ((s[sStart+3]&0xFF)-0x30) + 0x10000;
        o[oStart] = (byte)(0xF0 | (u>>18));
        o[oStart+1] = (byte)(0x80 | ((u>>12)&0x3F));
        o[oStart+2] = (byte)(0x80 | ((u>>6)&0x3F));
        o[oStart+3] = (byte)(0x80 | (u&0x3F));
        return 4;
    }

    public static int funSioFromGB18030(byte[] statep, byte[] s, int sStart, int l, int info, byte[] o, int oStart, int osize)
    {
        long diff = info >> 8;
        long u;    /* Unicode Scalar Value */
        if ((diff & 0x20000) != 0) { /* GB18030 4 bytes */
            u = (((s[sStart]&0xFF)*10+(s[sStart+1]&0xFF))*126+(s[sStart+2] & 0xFF))*10+(s[sStart+3]&0xFF) - diff - 0x170000;
        }
        else { /* GB18030 2 bytes */
            u = (s[sStart]&0xFF)*256 + (s[sStart+1]&0xFF) + 24055 - diff;
        }
        o[oStart] = (byte)(0xE0 | (u>>12));
        o[oStart+1] = (byte)(0x80 | ((u>>6)&0x3F));
        o[oStart+2] = (byte)(0x80 | (u&0x3F));
        return 3;
    }

    public static int funSoToGB18030(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int osize)
    {
        long u = ((s[sStart]&0x07)<<18) | ((s[sStart+1]&0x3F)<<12) | ((s[sStart+2]&0x3F)<<6) | (s[sStart+3]&0x3F);
        u -= 0x10000;
        o[3] = (byte)(0x30 + u%10);
        u /= 10;
        o[2] = (byte)(0x81 + u%126);
        u /= 126;
        o[1] = (byte)(0x30 + u%10);
        o[0] = (byte)(0x90 + u/10);
        return 4;
    }

    public static int funSioToGB18030(byte[] statep, byte[] s, int sStart, int l, int info, byte[] o, int oStart, int osize)
    {
        long diff = info >> 8;
        long u;    /* Unicode Scalar Value */

        u = ((s[sStart]&0x0F)<<12) | ((s[sStart+1]&0x3F)<<6) | (s[sStart+2]&0x3F);

        if ((diff & 0x20000) != 0) { /* GB18030 4 bytes */
            u += (diff + 0x170000);
            u -= 1688980;
            u += 0x2;
            o[oStart+3] = (byte)(0x30 + u%10);
            u /= 10;
            u += 0x32;
            o[oStart+2] = (byte)(0x81 + u%126);
            u /= 126;
            u += 0x1;
            o[oStart+1] = (byte)(0x30 + u%10);
            u /= 10;
            o[oStart] = (byte)(0x81 + u);
            return 4;
        }
        else { /* GB18030 2 bytes */
            u += (diff - 24055);
            o[oStart+1] = (byte)(u%256);
            o[oStart] = (byte)(u/256);
            return 2;
        }
    }
}
