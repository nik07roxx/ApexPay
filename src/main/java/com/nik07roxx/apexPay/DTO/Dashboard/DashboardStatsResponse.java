package com.nik07roxx.apexPay.DTO.Dashboard;

import java.math.BigDecimal;

public record DashboardStatsResponse (
        Long numberOfUsers,
        Long numberOfCustomers,
        BigDecimal totalAccountBalance
){
}
