package com.tqmall.search.util;

import org.elasticsearch.search.SearchHit;
import org.lionsoul.jcseg.filter.ENSCFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * vin码搜索
 * Created by wcong on 14-7-9.
 */
public class Vin {

    private final static Logger logger = LoggerFactory.getLogger(Vin.class);

    private final static Integer vinLength = 17;

    public static boolean isVin(String string) {
        if (string == null) {
            return false;
        }
        if (string.length() != vinLength) {
            return false;
        }
        for (char c : string.toCharArray()) {
            if (FullHalfConverter.isFullChar(c)
                    || ENSCFilter.getEnCharType(c) == ENSCFilter.EN_NUMERIC
                    || ENSCFilter.isLowerCaseLetter(c)
                    || ENSCFilter.isUpperCaseLetter(c)
                    ) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 如果是车型 转入车型的搜索
     *
     * @param string
     * @return
     */
    public static String carModel(String string) {
        if (string == null || string.trim().length() == 0) {
            return null;
        }
        String[] splitArray = string.split(" +");
        if (splitArray.length != 4) {
            return null;
        }
        Map<String, String[]> fqMap = new HashMap<String, String[]>();
        fqMap.put("brand", new String[]{splitArray[0]});
        fqMap.put("series", new String[]{splitArray[1]});
        fqMap.put("power", new String[]{splitArray[2]});
        fqMap.put("year", new String[]{splitArray[3]});
        String[] rFields = {"year_id"};
        SearchHit[] searchRet = ElasticFactory.commonSearch(
                IndexNameType.SHOP_CAR_ALL,
                fqMap,
                rFields);
        return searchRet.length > 0 ? searchRet[0].field("year_id").getValue().toString() : null;
    }

    public static String getCarModelByLid(String lid) {
        if (lid == null) {
            return null;
        }
        Map<String, String[]> fqMap = new HashMap<>();
        fqMap.put("lid", new String[]{lid});
        String[] rFields = {"year_id"};
        SearchHit[] searchRet = ElasticFactory.commonSearch(
                IndexNameType.SHOP_CAR_ALL,
                fqMap,
                rFields);
        return searchRet.length > 0 ? searchRet[0].field("year_id").getValue().toString() : null;

    }

    /**
     * TODO: 根据前面的代码, 这而拿到的是yearId, 这个需要跟白锐确认一下是否需要那carId
     */
    public static Collection<String> getGoodsIdByCarModel(String[] carModel) {
        if (carModel == null || carModel.length == 0) {
            return new LinkedList<>();
        }
        logger.info("根据carType获取对应goodsId");
        Map<String, String[]> fqMap = new HashMap<>();
        fqMap.put("car_model", carModel);
        String[] rFields = {"goods_id"};
        SearchHit[] searchRet = ElasticFactory.commonSearch(
                IndexNameType.SHOP_GOODS_CAR,
                fqMap,
                rFields);
        Set<String> goodsIdSet = new HashSet<>();
        for (SearchHit searchHit : searchRet) {
            String goodsId = searchHit.field("goods_id").getValue().toString();
            goodsIdSet.add(goodsId);
        }
        return goodsIdSet;
    }

}
