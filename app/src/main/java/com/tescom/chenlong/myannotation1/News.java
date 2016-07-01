package com.tescom.chenlong.myannotation1;

import com.chenlong.anno.Column;
import com.chenlong.anno.Format;
import com.chenlong.anno.TableName;
import com.chenlong.base.BaseBean;
import com.chenlong.base.CommonUtils;

import java.util.Date;

@TableName(MyOpenHelper.TABLE_NEWS_NAME)
public class News extends BaseBean {
    private static final long serialVersionUID = 1L;
    @Column(MyOpenHelper.TABLE_NEWS_TITLE)
    public String title;
    @Column(MyOpenHelper.TABLE_NEW_SUMMARY)
    public String summary;
    @Format("yyyy:MM:dd")
    @Column(MyOpenHelper.TABLE_NEW_DATE)
    public Date date;
    @Column(MyOpenHelper.TABLE_NEW_SCORE1)
    public Float score1;
    @Column(MyOpenHelper.TABLE_NEW_SCORE2)
    public Double score2;

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "News [id=" + id + ", title=" + title + ", summary=" + summary
                + ", date=" + CommonUtils.date2string(date, "yyyy-MM-dd") + ", score1=" + score1 + ", score2="
                + score2 + "]";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Float getScore1() {
        return score1;
    }

    public void setScore1(Float score1) {
        this.score1 = score1;
    }

    public Double getScore2() {
        return score2;
    }

    public void setScore2(Double score2) {
        this.score2 = score2;
    }

}