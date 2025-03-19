package com.fabricService.repository;

import com.fabricService.domain.UserContract;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserContractRepository extends JpaRepository<UserContract, Long> {

    // 查找某个 uid 参与的所有合约 contract_id（无论是 A 还是 B）
    @Query("""
        SELECT uc.contractId FROM UserContract uc 
        WHERE uc.partyAUid = :uid OR uc.partyBUid = :uid
    """)
    List<Long> findContractIdsByUid(@Param("uid") Long uid);

    // 查找某个 uid 是 A 方的合约
    @Query("""
        SELECT uc.contractId FROM UserContract uc 
        WHERE uc.partyAUid = :uid
    """)
    List<Long> findContractIdsByPartyA(@Param("uid") Long uid);

    // 查找某个 uid 是 B 方的合约
    @Query("""
        SELECT uc.contractId FROM UserContract uc 
        WHERE uc.partyBUid = :uid
    """)
    List<Long> findContractIdsByPartyB(@Param("uid") Long uid);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO contract (partyA_uid, partyB_uid, created_at)
        VALUES (?1, ?2, CURRENT_TIMESTAMP)
    """, nativeQuery = true)
    void insertContract(Long partyAUid, Long partyBUid);
    // 获取最新插入的 contract_id
    @Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
    Long getLastContractId();


    @Modifying
    @Transactional
    @Query("DELETE FROM UserContract uc WHERE uc.contractId = :contractId")
    void deleteByContractId(@Param("contractId") Long contractId);

}
