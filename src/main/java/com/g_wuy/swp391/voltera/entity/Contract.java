package com.g_wuy.swp391.voltera.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "contract")
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id")
    private Post postid;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id")
    @JsonIgnoreProperties({"contractsBought", "contractsSold"})
    private User sellerid;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "buyer_id")
    @JsonIgnoreProperties({"contractsBought", "contractsSold"})
    private User buyerid;

    @Column(name = "contract_file", length = Integer.MAX_VALUE)
    private String contractfile;

    @Size(max = 20)
    @ColumnDefault("'PENDING'")
    @Column(name = "contract_status", length = 20)
    private String contractstatus;

    @Column(name = "signed_date")
    private LocalDate signeddate;

    @Column(name = "expiration_date")
    private LocalDate expirationdate;

    @Column(name = "seller_signed")
    private Boolean sellersigned;

    @Column(name = "buyer_signed")
    private Boolean buyersigned;

    @Column(name = "terms", length = Integer.MAX_VALUE)
    private String terms;
    @OneToMany(mappedBy = "contractid", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Transaction> transactions;
}