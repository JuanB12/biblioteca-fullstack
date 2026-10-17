package com.juanbarrios.biblioteca.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.loan")
public class LoanProperties {

    private int defaultDurationDays = 14;
    private int reminderDaysBeforeDue = 2;
    private int maxOverdueCount = 3;
    private int overdueWindowDays = 90;
    private int blockDurationDays = 7;

    public int getDefaultDurationDays() {
        return defaultDurationDays;
    }

    public void setDefaultDurationDays(int defaultDurationDays) {
        this.defaultDurationDays = defaultDurationDays;
    }

    public int getReminderDaysBeforeDue() {
        return reminderDaysBeforeDue;
    }

    public void setReminderDaysBeforeDue(int reminderDaysBeforeDue) {
        this.reminderDaysBeforeDue = reminderDaysBeforeDue;
    }

    public int getMaxOverdueCount() {
        return maxOverdueCount;
    }

    public void setMaxOverdueCount(int maxOverdueCount) {
        this.maxOverdueCount = maxOverdueCount;
    }

    public int getOverdueWindowDays() {
        return overdueWindowDays;
    }

    public void setOverdueWindowDays(int overdueWindowDays) {
        this.overdueWindowDays = overdueWindowDays;
    }

    public int getBlockDurationDays() {
        return blockDurationDays;
    }

    public void setBlockDurationDays(int blockDurationDays) {
        this.blockDurationDays = blockDurationDays;
    }
}
