package com.auction.client.core.config;

/**
 * Backward-compatible facade for old client imports.
 *
 * <p>The canonical action contract now lives in {@link
 * com.auction.shared.protocol.ActionConstants}. New code should import the shared class.
 */
@Deprecated(forRemoval = false)
public final class ActionConstants {

  private ActionConstants() {}

  public static final String AUTH_LOGIN = com.auction.shared.protocol.ActionConstants.AUTH_LOGIN;
  public static final String AUTH_REGISTER =
      com.auction.shared.protocol.ActionConstants.AUTH_REGISTER;

  public static final String AUCTION_GET_LIST =
      com.auction.shared.protocol.ActionConstants.AUCTION_GET_LIST;
  public static final String AUCTION_GET_DETAIL =
      com.auction.shared.protocol.ActionConstants.AUCTION_GET_DETAIL;
  public static final String AUCTION_SUBSCRIBE =
      com.auction.shared.protocol.ActionConstants.AUCTION_SUBSCRIBE;
  public static final String AUCTION_UNSUBSCRIBE =
      com.auction.shared.protocol.ActionConstants.AUCTION_UNSUBSCRIBE;

  public static final String BID_PLACE_BID =
      com.auction.shared.protocol.ActionConstants.BID_PLACE_BID;
  public static final String BID_GET_HISTORY =
      com.auction.shared.protocol.ActionConstants.BID_GET_HISTORY;

  public static final String WALLET_DEPOSIT =
      com.auction.shared.protocol.ActionConstants.WALLET_DEPOSIT;
  public static final String WALLET_GET_SUMMARY =
      com.auction.shared.protocol.ActionConstants.WALLET_GET_SUMMARY;
  public static final String WALLET_GET_TRANSACTIONS =
      com.auction.shared.protocol.ActionConstants.WALLET_GET_TRANSACTIONS;

  public static final String SELLER_ITEM_LIST_MY =
      com.auction.shared.protocol.ActionConstants.SELLER_ITEM_LIST_MY;
  public static final String SELLER_ITEM_CREATE =
      com.auction.shared.protocol.ActionConstants.SELLER_ITEM_CREATE;
  public static final String SELLER_ITEM_UPDATE =
      com.auction.shared.protocol.ActionConstants.SELLER_ITEM_UPDATE;
  public static final String SELLER_ITEM_DELETE =
      com.auction.shared.protocol.ActionConstants.SELLER_ITEM_DELETE;

  public static final String EVENT_BID_UPDATED =
      com.auction.shared.protocol.ActionConstants.EVENT_BID_UPDATED;
  public static final String EVENT_AUCTION_STATUS_CHANGED =
      com.auction.shared.protocol.ActionConstants.EVENT_AUCTION_STATUS_CHANGED;
}
