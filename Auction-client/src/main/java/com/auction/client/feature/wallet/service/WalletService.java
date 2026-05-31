package com.auction.client.feature.wallet.service;

import com.auction.shared.dto.wallet.WalletSummaryDto;
import com.auction.shared.dto.wallet.WalletTransactionDto;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
  WalletSummaryDto getSummary() throws IOException;

  WalletSummaryDto deposit(BigDecimal amount) throws IOException;

  List<WalletTransactionDto> getTransactions(int limit) throws IOException;
}
