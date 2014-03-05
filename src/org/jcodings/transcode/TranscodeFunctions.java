package org.jcodings.transcode;

import java.util.Arrays;

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

    public static int iso2022jpInit(byte[] state) {
        Arrays.fill(state, G0_ASCII);
        return 0;
    }

    public static final byte G0_ASCII = 0;
    public static final byte G0_JISX0208_1978 = 1;
    public static final byte G0_JISX0208_1983 = 2;
    public static final byte G0_JISX0201_KATAKANA = 3;

    public static final byte EMACS_MULE_LEADING_CODE_JISX0208_1978 = (byte)0220;
    public static final byte EMACS_MULE_LEADING_CODE_JISX0208_1983 = (byte)0222;
    
    public static final byte[] tbl0208 = {
                    (byte)0x21, (byte)0x23, (byte)0x21, (byte)0x56, (byte)0x21, (byte)0x57, (byte)0x21, (byte)0x22, (byte)0x21, (byte)0x26, (byte)0x25, (byte)0x72, (byte)0x25, (byte)0x21, (byte)0x25, (byte)0x23,
                    (byte)0x25, (byte)0x25, (byte)0x25, (byte)0x27, (byte)0x25, (byte)0x29, (byte)0x25, (byte)0x63, (byte)0x25, (byte)0x65, (byte)0x25, (byte)0x67, (byte)0x25, (byte)0x43, (byte)0x21, (byte)0x3C,
                    (byte)0x25, (byte)0x22, (byte)0x25, (byte)0x24, (byte)0x25, (byte)0x26, (byte)0x25, (byte)0x28, (byte)0x25, (byte)0x2A, (byte)0x25, (byte)0x2B, (byte)0x25, (byte)0x2D, (byte)0x25, (byte)0x2F,
                    (byte)0x25, (byte)0x31, (byte)0x25, (byte)0x33, (byte)0x25, (byte)0x35, (byte)0x25, (byte)0x37, (byte)0x25, (byte)0x39, (byte)0x25, (byte)0x3B, (byte)0x25, (byte)0x3D, (byte)0x25, (byte)0x3F,
                    (byte)0x25, (byte)0x41, (byte)0x25, (byte)0x44, (byte)0x25, (byte)0x46, (byte)0x25, (byte)0x48, (byte)0x25, (byte)0x4A, (byte)0x25, (byte)0x4B, (byte)0x25, (byte)0x4C, (byte)0x25, (byte)0x4D,
                    (byte)0x25, (byte)0x4E, (byte)0x25, (byte)0x4F, (byte)0x25, (byte)0x52, (byte)0x25, (byte)0x55, (byte)0x25, (byte)0x58, (byte)0x25, (byte)0x5B, (byte)0x25, (byte)0x5E, (byte)0x25, (byte)0x5F,
                    (byte)0x25, (byte)0x60, (byte)0x25, (byte)0x61, (byte)0x25, (byte)0x62, (byte)0x25, (byte)0x64, (byte)0x25, (byte)0x66, (byte)0x25, (byte)0x68, (byte)0x25, (byte)0x69, (byte)0x25, (byte)0x6A,
                    (byte)0x25, (byte)0x6B, (byte)0x25, (byte)0x6C, (byte)0x25, (byte)0x6D, (byte)0x25, (byte)0x6F, (byte)0x25, (byte)0x73, (byte)0x21, (byte)0x2B, (byte)0x21, (byte)0x2C};

    public static int funSoCp50220Encoder(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        int output0 = oStart;
        byte[] sp = statep;

        if (sp[0] == G0_JISX0201_KATAKANA) {
            int c = sp[2] & 0x7F;
            int p = (c - 0x21) * 2;
            byte[] pBytes = tbl0208;
            if (sp[1] == G0_JISX0208_1983) {
                o[oStart++] = 0x1B;
                o[oStart++] = (byte)'$';
                o[oStart++] = (byte)'B';
            }
            sp[0] = G0_JISX0208_1983;
            o[oStart++] = pBytes[p++];
            if (l == 2 && (s[sStart] & 0xFF) == 0x8E) {
                if ((s[sStart+1] & 0xFF) == 0xDE) {
                    o[oStart++] = (byte)(pBytes[p++] + 2);
                    return oStart - output0;
                }
            }
            o[oStart++] = pBytes[p];
        }

        if (l == 2 && (s[sStart] & 0xFF) == 0x8E) {
            int p = ((s[sStart+1] & 0xFF) - 0xA1) * 2;
            byte[] pBytes = tbl0208;
            if ((0xA1 <= (s[sStart+1] & 0xFF) && (s[sStart+1] & 0xFF) <= 0xB5) ||
                    (0xC5 <= (s[sStart+1] & 0xFF) && (s[sStart+1] & 0xFF) <= 0xC9) ||
                    (0xCF <= (s[sStart+1] & 0xFF) && (s[sStart+1] & 0xFF) <= 0xDF)) {
                if (sp[0] != G0_JISX0208_1983) {
                    o[oStart++] = 0x1b;
                    o[oStart++] = '$';
                    o[oStart++] = 'B';
                    sp[0] = G0_JISX0208_1983;
                }
                o[oStart++] = pBytes[p++];
                o[oStart++] = pBytes[p];
                return oStart - output0;
            }

            sp[2] = s[1];
            sp[1] = sp[0];
            sp[0] = G0_JISX0201_KATAKANA;
            return oStart - output0;
        }

        oStart += funSoCp5022xEncoder(statep, s, sStart, l, o, oStart, oSize);
        return oStart - output0;
    }

    public static int funSoCp5022xEncoder(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        byte[] sp = statep;
        int output0 = oStart;
        int newstate;

        if (l == 1)
            newstate = G0_ASCII;
        else if (s[sStart] == 0x8E) {
            sStart++;
            l = 1;
            newstate = G0_JISX0201_KATAKANA;
        }
        else
            newstate = G0_JISX0208_1983;

        if (sp[0] != newstate) {
            if (newstate == G0_ASCII) {
                o[oStart++] = 0x1b;
                o[oStart++] = '(';
                o[oStart++] = 'B';
            }
            else if (newstate == G0_JISX0201_KATAKANA) {
                o[oStart++] = 0x1b;
                o[oStart++] = '(';
                o[oStart++] = 'I';
            }
            else {
                o[oStart++] = 0x1b;
                o[oStart++] = '$';
                o[oStart++] = 'B';
            }
            sp[0] = (byte)newstate;
        }

        if (l == 1) {
            o[oStart++] = (byte)(s[sStart] & 0x7f);
        }
        else {
            o[oStart++] = (byte)(s[sStart] & 0x7f);
            o[oStart++] = (byte)(s[sStart+1] & 0x7f);
        }

        return oStart - output0;
    }

    public static int finishCp50220Encoder(byte[] statep, byte[] o, int oStart, int size) {
        byte[] sp = statep;
        int output0 = oStart;

        if (sp[0] == G0_ASCII) return 0;

        if (sp[0] == G0_JISX0201_KATAKANA) {
            int c = sp[2] & 0x7F;
            int p = (c - 0x21) * 2;
            byte[] pBytes = tbl0208;
            if (sp[1] != G0_JISX0208_1983) {
                o[oStart++] = 0x1b;
                o[oStart++] = '$';
                o[oStart++] = 'B';
            }
            sp[0] = G0_JISX0208_1983;
            o[oStart++] = pBytes[p++];
            o[oStart++] = pBytes[p];
        }

        o[oStart++] = 0x1b;
        o[oStart++] = '(';
        o[oStart++] = 'B';
        sp[0] = G0_ASCII;

        return oStart - output0;
    }

    public static int iso2022jpEncoderResetSequenceSize(byte[] statep) {
        byte[] sp = statep;
        if (sp[0] != G0_ASCII) return 3;
        return 0;
    }

    public static final int iso2022jp_decoder_jisx0208_rest = Transcoding.WORDINDEX2INFO(16);

    public static int funSiIso50220jpDecoder(byte[] statep, byte[] s, int sStart, int l) {
        byte[] sp = statep;
        if (sp[0] == G0_ASCII)
        return TranscodingInstruction.NOMAP;
        else if (0x21 <= s[0] && s[0] <= 0x7e)
            return iso2022jp_decoder_jisx0208_rest;
        else
            return TranscodingInstruction.INVALID;
    }

    public static int funSoIso50220jpDecoder(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        byte[] sp = statep;
        if (s[sStart] == 0x1b) {
            if (s[sStart+1] == '(') {
                switch (s[sStart+l-1]) {
                    case 'B':
                    case 'J':
                        sp[0] = G0_ASCII;
                        break;
                }
            }
            else {
                switch (s[l-1]) {
                    case '@':
                        sp[0] = G0_JISX0208_1978;
                        break;

                    case 'B':
                        sp[0] = G0_JISX0208_1983;
                        break;
                }
            }
            return 0;
        }
        else {
            if (sp[0] == G0_JISX0208_1978) {
                o[oStart] = EMACS_MULE_LEADING_CODE_JISX0208_1978;
            } else {
                o[oStart] = EMACS_MULE_LEADING_CODE_JISX0208_1983;
            }
            o[oStart+1] = (byte)(s[sStart] | 0x80);
            o[oStart+2] = (byte)(s[sStart+1] | 0x80);
            return 3;
        }
    }

    public static int funSoStatelessIso2022jpToEucjp(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        o[oStart] = s[sStart+1];
        o[oStart+1] = s[sStart+2];
        return 2;
    }

    public static int funSoEucjpToStatelessIso2022jp(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        o[oStart] = EMACS_MULE_LEADING_CODE_JISX0208_1983;
        o[oStart+1] = s[sStart];
        o[oStart+2] = s[sStart+1];
        return 3;
    }

    public static int funSoIso2022jpEncoder(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        byte[] sp = statep;
        int output0 = oStart;
        int newstate;

        if (l == 1)
            newstate = G0_ASCII;
        else if (s[0] == EMACS_MULE_LEADING_CODE_JISX0208_1978)
            newstate = G0_JISX0208_1978;
        else
            newstate = G0_JISX0208_1983;

        if (sp[0] != newstate) {
            if (newstate == G0_ASCII) {
                o[oStart++] = 0x1b;
                o[oStart++] = '(';
                o[oStart++] = 'B';
            }
            else if (newstate == G0_JISX0208_1978) {
                o[oStart++] = 0x1b;
                o[oStart++] = '$';
                o[oStart++] = '@';
            }
            else {
                o[oStart++] = 0x1b;
                o[oStart++] = '$';
                o[oStart++] = 'B';
            }
            sp[0] = (byte)newstate;
        }

        if (l == 1) {
            o[oStart++] = (byte)(s[sStart] & 0x7f);
        }
        else {
            o[oStart++] = (byte)(s[sStart+1] & 0x7f);
            o[oStart++] = (byte)(s[sStart+2] & 0x7f);
        }

        return oStart - output0;
    }

    public static int finishIso2022jpEncoder(byte[] statep, byte[] o, int oStart, int oSize) {
        byte[] sp = statep;
        int output0 = oStart;

        if (sp[0] == G0_ASCII) return 0;

        o[oStart++] = 0x1b;
        o[oStart++] = '(';
        o[oStart++] = 'B';
        sp[0] = G0_ASCII;

        return oStart - output0;
    }

    public static int funSiCp50220Decoder(byte[] statep, byte[] s, int sStart, int l) {
        byte[] sp = statep;
        int c;
        int s0 = s[sStart] & 0xFF;
        switch (sp[0]) {
            case G0_ASCII:
                if (0xA1 <= s0 && s0 <= 0xDF)
                    return TranscodingInstruction.FUNso;
                return TranscodingInstruction.NOMAP;
            case G0_JISX0201_KATAKANA:
                c = s[sStart] & 0x7F;
                if (0x21 <= c && c <= 0x5f)
                    return TranscodingInstruction.FUNso;
                break;
            case G0_JISX0208_1978:
                if ((0x21 <= s0 && s0 <= 0x28) || (0x30 <= s0 && s0 <= 0x74))
                    return iso2022jp_decoder_jisx0208_rest;
                break;
            case G0_JISX0208_1983:
                if ((0x21 <= s0 && s0 <= 0x28) ||
                        s0 == 0x2D ||
                        (0x30 <= s0 && s0 <= 0x74) ||
                        (0x79 <= s0 && s0 <= 0x7C)) {
                    /* 0x7F <= s0 && s0 <= 0x92) */
                    return iso2022jp_decoder_jisx0208_rest;
                }
                break;
        }
        return TranscodingInstruction.INVALID;
    }

    public static int funSoCp50220Decoder(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        byte[] sp = statep;
        switch (s[sStart]&0xFF) {
            case 0x1b:
                if ((s[sStart+1]&0xFF) == '(') {
                    switch ((s[sStart+l-1]&0xFF)) {
                        case 'B':
                        case 'J':
                            sp[0] = G0_ASCII;
                            break;
                        case 'I':
                            sp[0] = G0_JISX0201_KATAKANA;
                            break;
                    }
                }
                else {
                    switch (s[sStart+l-1]&0xFF) {
                        case '@':
                            sp[0] = G0_JISX0208_1978;
                            break;
                        case 'B':
                            sp[0] = G0_JISX0208_1983;
                            break;
                    }
                }
                return 0;
            case 0x0E:
                sp[0] = G0_JISX0201_KATAKANA;
                return 0;
            case 0x0F:
                sp[0] = G0_ASCII;
                return 0;
            default:
                if (sp[0] == G0_JISX0201_KATAKANA ||
                    (0xA1 <= (s[sStart]&0xFF) && (s[sStart]&0xFF) <= 0xDF && sp[0] == G0_ASCII)) {
                o[oStart] = (byte)0x8E;
                o[oStart+1] = (byte)(s[sStart] | 0x80);
            }
        /* else if (0x7F == s[0] && s[0] <= 0x88) { */
            /* User Defined Characters */
            /* o[n++] = s[0] | 0xE0; */
            /* o[n++] = s[1] | 0x80; */
        /* else if (0x89 <= s[0] && s[0] <= 0x92) { */
            /* User Defined Characters 2 */
            /* o[n++] = 0x8f; */
            /* o[n++] = s[0] + 0x6C; */
            /* o[n++] = s[1] | 0x80; */
        /* } */
            else {
            /* JIS X 0208 */
            /* NEC Special Characters */
            /* NEC-selected IBM extended Characters */
                o[oStart] = (byte)(s[sStart] | 0x80);
                o[oStart+1] = (byte)(s[sStart+1] | 0x80);
            }
            return 2;
        }
    }

    public static int iso2022jpKddiInit(byte[] statep) {
        Arrays.fill(statep, G0_ASCII);
        return 0;
    }

    public static final int iso2022jp_kddi_decoder_jisx0208_rest = Transcoding.WORDINDEX2INFO(16);

    public static int funSiIso2022jpKddiDecoder(byte[] statep, byte[] s, int sStart, int l) {
        byte[] sp = statep;
        if (sp[0] == G0_ASCII) {
            return TranscodingInstruction.NOMAP;
        } else if (0x21 <= (s[sStart]&0xFF) && (s[sStart]&0xFF) <= 0x7e) {
            return iso2022jp_kddi_decoder_jisx0208_rest;
        } else {
            return TranscodingInstruction.INVALID;
        }
    }

    public static int funSoIso2022jpKddiDecoder(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        byte[] sp = statep;
        if ((s[sStart]&0xFF) == 0x1b) {
            if (s[sStart+1] == '(') {
                switch (s[sStart+l-1] & 0xFF) {
                    case 'B': /* US-ASCII */
                    case 'J': /* JIS X 0201 Roman */
                        sp[0] = G0_ASCII;
                        break;
                }
            }
            else {
                switch (s[sStart+l-1] & 0xFF) {
                    case '@':
                        sp[0] = G0_JISX0208_1978;
                        break;

                    case 'B':
                        sp[0] = G0_JISX0208_1983;
                        break;
                }
            }
            return 0;
        }
        else {
            if (sp[0] == G0_JISX0208_1978) {
                o[oStart] = EMACS_MULE_LEADING_CODE_JISX0208_1978;
            } else {
                o[oStart] = EMACS_MULE_LEADING_CODE_JISX0208_1983;
            }
            o[oStart+1] = (byte)(s[sStart] | 0x80);
            o[oStart+2] = (byte)(s[sStart+1] | 0x80);
            return 3;
        }
    }

    public static int funSoIso2022jpKddiEncoder(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        byte[] sp = statep;
        int output0 = oStart;
        int newstate;

        if (l == 1)
            newstate = G0_ASCII;
        else if (s[0] == EMACS_MULE_LEADING_CODE_JISX0208_1978)
            newstate = G0_JISX0208_1978;
        else
            newstate = G0_JISX0208_1983;

        if (sp[0] != newstate) {
            o[oStart++] = 0x1b;
            switch (newstate) {
                case G0_ASCII:
                    o[oStart++] = '(';
                    o[oStart++] = 'B';
                    break;
                case G0_JISX0208_1978:
                    o[oStart++] = '$';
                    o[oStart++] = '@';
                    break;
                default:
                    o[oStart++] = '$';
                    o[oStart++] = 'B';
                    break;
            }
            sp[0] = (byte)newstate;
        }

        if (l == 1) {
            o[oStart++] = (byte)(s[sStart] & 0x7f);
        }
        else {
            o[oStart++] = (byte)(s[sStart+1] & 0x7f);
            o[oStart++] = (byte)(s[sStart+2] & 0x7f);
        }

        return oStart - output0;

    }

    public static int finishIso2022jpKddiEncoder(byte[] statep, byte[] o, int oStart, int oSize) {
        byte[] sp = statep;
        int output0 = oStart;

        if (sp[0] == G0_ASCII)
        return 0;

        o[oStart++] = 0x1b;
        o[oStart++] = '(';
        o[oStart++] = 'B';
        sp[0] = G0_ASCII;

        return oStart - output0;
    }

    public static int iso2022jpKddiEncoderResetSequence_size(byte[] statep) {
        byte[] sp = statep;
        if (sp[0] != G0_ASCII) return 3;
        return 0;
    }
}
