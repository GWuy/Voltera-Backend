package com.g_wuy.swp391.voltera.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponse {
    private Integer contractId;
    private Integer postId;
    private String buyerName;
    private String sellerName;
    private String buyerEmail;
    private String sellerEmail;
    private String postTitle;
    private String terms;
    private Boolean signedByBuyer;
    private Boolean signedBySeller;
    private String contractStatus;
    private LocalDate signedDate;
    private String contractFile;
    private String transactionStatus;
}
