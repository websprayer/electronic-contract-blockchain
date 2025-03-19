package com.fabricService.domain;

import jakarta.persistence.*;
import java.sql.Timestamp;
import lombok.Data;

@Data
@Entity
@Table(name = "contract")
public class UserContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_id")
    private Long contractId;  // 现在 contract_id 是主键

    @Column(name = "partyA_uid", nullable = false)
    private Long partyAUid;

    @Column(name = "partyB_uid", nullable = false)
    private Long partyBUid;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;
}
