package io.smallrye.graphql.tests.client.parsing;

import java.util.Date;

import javax.json.bind.annotation.JsonbDateFormat;

import org.eclipse.microprofile.graphql.DateFormat;
import org.eclipse.microprofile.graphql.NumberFormat;

public class ObjectWithFormattedFields {

    // choose a somewhat crazy format to make sure it can't be parsed "by mistake" without proper knowledge of the format
    @DateFormat("MMM yyyy dd HH,mm,ss MMM EEE")
    private Date date;

    // the same should work with JSON-B annotations
    @JsonbDateFormat("MMM yyyy dd HH,mm,ss MMM EEE")
    private Date dateWithJsonbAnnotation;

    @NumberFormat(locale = "in")
    private Double doubleNumber;

    @NumberFormat(locale = "in")
    private Long longNumber;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getDoubleNumber() {
        return doubleNumber;
    }

    public void setDoubleNumber(Double doubleNumber) {
        this.doubleNumber = doubleNumber;
    }

    public Long getLongNumber() {
        return longNumber;
    }

    public void setLongNumber(Long longNumber) {
        this.longNumber = longNumber;
    }

    public Date getDateWithJsonbAnnotation() {
        return dateWithJsonbAnnotation;
    }

    public void setDateWithJsonbAnnotation(Date dateWithJsonbAnnotation) {
        this.dateWithJsonbAnnotation = dateWithJsonbAnnotation;
    }
}
