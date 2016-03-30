package com.tqmall.search.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by 刘一波 on 16/2/23.
 * E-Mail:yibo.liu@tqmall.com
 */
public enum IndexNameType {
    SHOP(),
    SHOP_GOODS("SHOP_GOODS", "GOODS", SHOP),
    SHOP_ATTRIBUTE("SHOP_ATTRIBUTE", "ATTRIBUTE", SHOP),
    SHOP_GOODS_STOCK("SHOP_GOODS_STOCK", "GOODS_STOCK", SHOP),
    SHOP_GOODS_WAREHOUSE("SHOP_GOODS_WAREHOUSE", "GOODS_WAREHOUSE", SHOP),
    SHOP_WAREHOUSE("SHOP_WAREHOUSE", "WAREHOUSE", SHOP),
    SHOP_GOODS_ATTR("SHOP_GOODS_ATTR", "GOODS_ATTR", SHOP),
    SHOP_CAR_ALL("SHOP_CAR_ALL", "CAR_ALL", SHOP),
    SHOP_CAR_VIN("SHOP_CAR_VIN", "CAR_VIN", SHOP),
    SHOP_GOODS_CAR("SHOP_GOODS_CAR", "GOODS_CAR", SHOP),
    SHOP_CAR_CATEGORY("SHOP_CAR_CATEGORY", "CAR_CATEGORY", SHOP),
    SHOP_SELLER_GOODS("SHOP_SELLER_GOODS", "SELLER_GOODS_FORM", SHOP),
    SHOP_SUGGEST("SHOP_SUGGEST", "SUGGEST", SHOP),
    SHOP_GOODS_QUALITY_PERIOD("SHOP_GOODS_QUALITY_PERIOD", "GOODS_QUALITY_PERIOD", SHOP),
    SHOP_ERP_INVENTORY("SHOP_ERP_INVENTORY", "ERP_INVENTORY", SHOP),
    SHOP_SALE_NUMBER("SHOP_SALE_NUMBER", "SALE_NUMBER", SHOP),
    SHOP_GOODS_THIRD_PART("", "", SHOP_GOODS_WAREHOUSE),
    SHOP_USER("", "", SHOP),

    SAINT(),
    SAINT_CUSTOMER_NODE_STATUS("", "", SAINT),
    SAINT_WAREHOUSE("", "", SAINT),
    ERP_ORDER_CHILD("", "", SAINT),
    ERP_ORDER_EXT("", "", SAINT),
    ERP_ORDER_GOODS("", "", SAINT),
    ERP_ORDER("", "", SAINT),

    CRM(),
    CRM_CUSTOMER("", "", CRM),
    CRM_CUSTOMER_OWN("", "", CRM),
    CRM_VIRTUAL_CUSTOMER_OWN("", "", CRM),
    CRM_CONTACTS("", "", CRM),
    CRM_TAG("", "", CRM),
    CRM_TAG_OBJ("", "", CRM),
    CRM_USER_INFO("", "", CRM),
    CRM_CUSTOMER_GROUP("", "", CRM),
    SAINT_CUSTOMER_FILE_PATH("", "", CRM),
    SAINT_CUSTOMER_JOIN_AUDIT("", "", CRM),

    ORDER(),
    ORDER_INFO("", "", ORDER),
    ORDER_INFO_EXT("", "", ORDER),
    GOODS_OF_ORDER("", "", ORDER),
    ORDER_GOODS("", "", ORDER),
    ORDER_USERS_EXT("", "", ORDER),
    ORDER_SELLER("", "", ORDER),
    ORDER_USER_PURCHASED_GOODS("", "", ORDER),

    OTHER(),
    OTHER_ORDER_ACTION_SALE_RETURN("", "", OTHER),

    LEGEND(),
    LEGEND_CHEZHU_COUPON_NOTICE("", "", LEGEND),
    LEGEND_SERVICE_GOODS_SUITE("", "", LEGEND),
    LEGEND_ORDER("", "", LEGEND),
    LEGEND_CUSTOMER("", "", LEGEND),
    LEGEND_CUSTOMER_CAR("", "", LEGEND),
    LEGEND_GOODS_CATEGARY("", "", LEGEND),
    LEGEND_GOODS("", "", LEGEND),
    LEGEND_SHOP("", "", LEGEND),
    LEGEND_SHOP_SERVER_CATEGARY("", "", LEGEND),
    LEGEND_SERVICE_TEMPLATE("", "", LEGEND),
    LEGEND_SERVICE_TEMPLATE_CATE_REL("", "", LEGEND),
    LEGEND_SHOP_SERVICE_TAG_REL("", "", LEGEND),
    LEGEND_SHOP_SERVER("", "", LEGEND),
    LEGEND_SHOP_MEMBER("", "", LEGEND),
    LEGEND_SHOP_MEMBER_SERVICE("", "", LEGEND),
    LEGEND_SHOP_MEMBER_SERVICE_SUITE("", "", LEGEND),
    LEGEND_SHOP_MEMBER_SERVICE_REL("", "", LEGEND),
    LEGEND_SHOP_MEMBER_SUITE_SERVICE_REL("", "", LEGEND),
    LEGEND_LIBRARY("", "", LEGEND),
    LEGEND_KNOW("", "", LEGEND),

    DM1(),
    SALES("DM1", "", DM1),

    WIND(),
    WIND_TIRE_RELEASE("", "", WIND),
    WIND_TIRE_RELEASE_PRICE("", "", WIND),
    WIND_TIRE_GOODS("", "", WIND),

    CONST(),
    REGION("", "", CONST),
    GOODS_ATTR_VALUE("", "", CONST),


    UCENTER(),
    UCENTER_ACCOUNT("", "", UCENTER),
    UCENTER_ADDRESS("", "", UCENTER),
    UCENTER_SHOP("", "", UCENTER),
    UCENTER_SHOP_MASTER_SLAVE("", "", UCENTER),
    UCENTER_SHOP_TAG("", "", UCENTER),
    UCENTER_TAG("", "", UCENTER),

    CLOUDEPC(),
    CLOUDEPC_CENTER_GOODS("", "", CLOUDEPC),

    MATCHER("MATCHER", "MATCHER", null);


    private String indexRealName;//本索引名-真实名,建索引用.ps:除了建索引的时候,其它时候它的值是空的
    private String indexName;//本索引名-别名,查询用
    private String indexType;//本索引类型
    private IndexNameType parentIndex;//父索引
    private boolean topLevel = false;

    final Logger log = LoggerFactory.getLogger(IndexNameType.class);

    IndexNameType() {
        indexRealName = "";
        indexName = "顶级不可用";
        indexType = "顶级不可用";
        topLevel = true;
    }

    IndexNameType(String indexName, String indexType, IndexNameType parentIndex) {
        if (StringUtils.isBlank(indexName))
            indexName = name().toLowerCase();
        if (StringUtils.isBlank(indexType))
            indexType = name().toLowerCase();
        this.indexRealName = "";
        this.indexName = indexName;
        this.indexType = indexType;
        this.parentIndex = parentIndex;
    }

    /**
     * 真实名称
     */
    public String getIndexRealName() {
        checkTopLevel();
        return indexRealName.toLowerCase();
    }

    public void cleanRealName() {
        setIndexRealName("");
    }

    public void setIndexRealName(String indexRealName) {
        this.indexRealName = indexRealName;
    }

    /**
     * 是否需要重新指定别名,当且仅当真实名称不为空且别名和真实名称不同时
     *
     * @return true 需要|false 不需要
     */
    public boolean needRenameAlias() {
        return StringUtils.isNotBlank(getIndexRealName()) && !getIndexRealName().equals(getIndexAlias());
    }

    /**
     * 别名
     */
    public String getIndexAlias() {
        checkTopLevel();
        return indexName.toLowerCase();
    }

    /**
     * 有名字时用名字，没名字时用别名
     */
    public String getIndexName() {
        if (StringUtils.isNotBlank(indexRealName)) {
            return indexRealName.toLowerCase();
        } else {
            return indexName.toLowerCase();
        }
    }

    private void checkTopLevel() {
        if (topLevel) {
            StackTraceElement stackTraceElement = new Throwable().getStackTrace()[2];
            if (!"com.tqmall.search.util.IndexNameType".equals(stackTraceElement.getClassName()))
                log.error("此类: " + stackTraceElement.getClassName() + "[" + stackTraceElement.getLineNumber() + "] 不可使用此枚举值,此枚举值[" + name() + "]仅用于赋值给子类的parentIndex属性");
        }
    }

    public String getIndexType() {
        checkTopLevel();
        return indexType.toLowerCase();
    }

    public IndexNameType getParentIndex() {
        return parentIndex;
    }

    @Override
    public String toString() {
        checkTopLevel();
        return "[" + super.toString() + ":" + this.getIndexAlias() + "," + this.getIndexRealName() + "," + this.getIndexType() + "]";
    }

    public static IndexNameType parse(String str) {
        return valueOf(IndexNameType.class, str.toUpperCase());
    }
}
