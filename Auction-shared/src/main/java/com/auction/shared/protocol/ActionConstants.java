package com.auction.shared.protocol;

/**
 * Contract action/event names shared by Client and Server.
 *
 * <p>This class is the single source of truth for socket action names. Client and server
 * should import constants from this class instead of hardcoding strings.</p>
 */
public final class ActionConstants {

    private ActionConstants() {
    }

    // ===== AUTH =====
    public static final String AUTH_LOGIN = "AUTH_LOGIN";
    public static final String AUTH_REGISTER = "AUTH_REGISTER";

    // ===== AUCTION =====
    public static final String AUCTION_GET_LIST = "AUCTION_GET_LIST";
    public static final String AUCTION_GET_DETAIL = "AUCTION_GET_DETAIL";
    public static final String AUCTION_SUBSCRIBE = "AUCTION_SUBSCRIBE";
    public static final String AUCTION_UNSUBSCRIBE = "AUCTION_UNSUBSCRIBE";
    public static final String CATEGORY_GET_LIST = "CATEGORY_GET_LIST";

    // ===== BIDDING =====
    public static final String BID_PLACE_BID = "BID_PLACE_BID";
    public static final String BID_GET_HISTORY = "BID_GET_HISTORY";

    // ===== WALLET =====
    public static final String WALLET_DEPOSIT = "WALLET_DEPOSIT";
    public static final String WALLET_GET_SUMMARY = "WALLET_GET_SUMMARY";
    public static final String WALLET_GET_TRANSACTIONS = "WALLET_GET_TRANSACTIONS";

    // ===== SELLER =====
    public static final String SELLER_ITEM_LIST_MY = "SELLER_ITEM_LIST_MY";
    public static final String SELLER_ITEM_CREATE = "SELLER_ITEM_CREATE";
    public static final String SELLER_ITEM_UPDATE = "SELLER_ITEM_UPDATE";
    public static final String SELLER_ITEM_DELETE = "SELLER_ITEM_DELETE";

    // ===== SERVER PUSH EVENTS =====
    public static final String EVENT_BID_UPDATED = "EVENT_BID_UPDATED";
    public static final String EVENT_AUCTION_STATUS_CHANGED = "EVENT_AUCTION_STATUS_CHANGED";
    public static final String EVENT_WALLET_UPDATED = "EVENT_WALLET_UPDATED";
}
