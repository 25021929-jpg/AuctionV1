package com.auction.server.feature.wallet.repository;

import com.auction.server.entity.WalletTransaction;
import java.util.List;

public interface WalletTransactionRepository {
    WalletTransaction save(WalletTransaction transaction);
    List<WalletTransaction> findByUser(Long userId, int limit);
}
