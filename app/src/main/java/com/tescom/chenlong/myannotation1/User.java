package com.tescom.chenlong.myannotation1;

import com.chenlong.anno.Column;
import com.chenlong.anno.Format;
import com.chenlong.anno.TableName;
import com.chenlong.base.BaseBean;

import java.util.Arrays;
import java.util.Date;

@TableName(MyOpenHelper.TABLE_USER_NAME)
public class User extends BaseBean {
    private static final long serialVersionUID = 1L;

    @Column(MyOpenHelper.TABLE_USER_BYTE1)
    public byte byte1;
    @Column(MyOpenHelper.TABLE_USER_BYTER1)
    public Byte byter1;
    @Column(MyOpenHelper.TABLE_USER_SHORT1)
    public short short1;
    @Column(MyOpenHelper.TABLE_USER_SHORTER1)
    public Short shorter1;

    @Column(MyOpenHelper.TABLE_USER_INT1)
    public int int1;
    @Column(MyOpenHelper.TABLE_USER_INTEGER1)
    public Integer inter1;

    @Column(MyOpenHelper.TABLE_USER_LONG1)
    public long long1;
    @Column(MyOpenHelper.TABLE_USER_LONGER1)
    public Long longer1;

    @Column(MyOpenHelper.TABLE_USER_FLOAT1)
    public float float1;
    @Column(MyOpenHelper.TABLE_USER_FLOATER1)
    public Float floater1;

    @Column(MyOpenHelper.TABLE_USER_DOUBLE1)
    public double double1;
    @Column(MyOpenHelper.TABLE_USER_DOUBLER1)
    public Double doubler1;

    @Column(MyOpenHelper.TABLE_USER_CH1)
    public char ch1;
    @Column(MyOpenHelper.TABLE_USER_CHARACTER1)
    public Character character1;

    @Column(MyOpenHelper.TABLE_USER_FLAG1)
    public boolean flag1;
    @Column(MyOpenHelper.TABLE_USER_FLAGER1)
    public Boolean flager1;

    @Column(MyOpenHelper.TABLE_USER_BYTES)
    public byte[] bytes;

    @Column(MyOpenHelper.TABLE_USER_STRING1)
    public String string1;

    @Column(MyOpenHelper.TABLE_USER_DATE)
    @Format
    public Date date;

    public byte getByte1() {
        return byte1;
    }

    public void setByte1(byte byte1) {
        this.byte1 = byte1;
    }

    public Byte getByter1() {
        return byter1;
    }

    public void setByter1(Byte byter1) {
        this.byter1 = byter1;
    }

    public short getShort1() {
        return short1;
    }

    public void setShort1(short short1) {
        this.short1 = short1;
    }

    public Short getShorter1() {
        return shorter1;
    }

    public void setShorter1(Short shorter1) {
        this.shorter1 = shorter1;
    }

    public int getInt1() {
        return int1;
    }

    public void setInt1(int int1) {
        this.int1 = int1;
    }

    public Integer getInter1() {
        return inter1;
    }

    public void setInter1(Integer inter1) {
        this.inter1 = inter1;
    }

    public long getLong1() {
        return long1;
    }

    public void setLong1(long long1) {
        this.long1 = long1;
    }

    public Long getLonger1() {
        return longer1;
    }

    public void setLonger1(Long longer1) {
        this.longer1 = longer1;
    }

    public float getFloat1() {
        return float1;
    }

    public void setFloat1(float float1) {
        this.float1 = float1;
    }

    public Float getFloater1() {
        return floater1;
    }

    public void setFloater1(Float floater1) {
        this.floater1 = floater1;
    }

    public double getDouble1() {
        return double1;
    }

    public void setDouble1(double double1) {
        this.double1 = double1;
    }

    public Double getDoubler1() {
        return doubler1;
    }

    public void setDoubler1(Double doubler1) {
        this.doubler1 = doubler1;
    }

    public char getCh1() {
        return ch1;
    }

    public void setCh1(char ch1) {
        this.ch1 = ch1;
    }

    public Character getCharacter1() {
        return character1;
    }

    public void setCharacter1(Character character1) {
        this.character1 = character1;
    }

    public boolean isFlag1() {
        return flag1;
    }

    public void setFlag1(boolean flag1) {
        this.flag1 = flag1;
    }

    public Boolean getFlager1() {
        return flager1;
    }

    public void setFlager1(Boolean flager1) {
        this.flager1 = flager1;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getString1() {
        return string1;
    }

    public void setString1(String string1) {
        this.string1 = string1;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "User [byte1=" + byte1 + ", byter1=" + byter1 + ", short1=" + short1 + ", shorter1=" + shorter1
                + ", int1=" + int1 + ", inter1=" + inter1 + ", long1=" + long1 + ", longer1=" + longer1 + ", float1="
                + float1 + ", floater1=" + floater1 + ", double1=" + double1 + ", doubler1=" + doubler1 + ", ch1=" + ch1
                + ", character1=" + character1 + ", flag1=" + flag1 + ", flager1=" + flager1 + ", bytes="
                + Arrays.toString(bytes) + ", string1=" + string1 + ", date=" + date + "]";
    }


}
