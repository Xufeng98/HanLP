/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/17 14:20</create-date>
 *
 * <copyright file="SuggesterEx.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.suggest;


import com.hankcs.hanlp.suggest.scorer.BaseScorer;
import com.hankcs.hanlp.suggest.scorer.IScorer;
import com.hankcs.hanlp.suggest.scorer.editdistance.EditDistanceScorer;
import com.hankcs.hanlp.suggest.scorer.lexeme.IdVector;
import com.hankcs.hanlp.suggest.scorer.lexeme.IdVectorScorer;
import com.hankcs.hanlp.suggest.scorer.pinyin.PinyinScorer;

import java.util.*;

/**
 * @author hankcs
 */
public class Suggester implements ISuggester
{
    List<BaseScorer> scorerList;

    public Suggester()
    {
        scorerList = new ArrayList<>();
        scorerList.add(new IdVectorScorer());
        scorerList.add(new EditDistanceScorer());
        scorerList.add(new PinyinScorer());
    }

    @Override
    public void addSentence(String sentence)
    {
        for (IScorer scorer : scorerList)
        {
            scorer.addSentence(sentence);
        }
    }

    @Override
    public List<String> suggest(String key, int size)
    {
        List<String> resultList = new ArrayList<>(size);
        TreeMap<String, Double> scoreMap = new TreeMap<>();
        for (BaseScorer scorer : scorerList)
        {
            Map<String, Double> map = scorer.computeScore(key);
            Double max = max(map);  // 用于正规化一个map
            for (Map.Entry<String, Double> entry : map.entrySet())
            {
                Double score = scoreMap.get(entry.getKey());
                if (score == null) score = 0.0;
                scoreMap.put(entry.getKey(), score / max + entry.getValue() * scorer.boost);
            }
        }
        for (Map.Entry<Double, Set<String>> entry : sortScoreMap(scoreMap).entrySet())
        {
            for (String sentence : entry.getValue())
            {
                if (resultList.size() >= size) return resultList;
                resultList.add(sentence);
            }
        }

        return resultList;
    }

    /**
     * 将分数map排序折叠
     * @param scoreMap
     * @return
     */
    private static TreeMap<Double ,Set<String>> sortScoreMap(TreeMap<String, Double> scoreMap)
    {
        TreeMap<Double, Set<String>> result = new TreeMap<>(Collections.reverseOrder());
        for (Map.Entry<String, Double> entry : scoreMap.entrySet())
        {
            Set<String> sentenceSet = result.get(entry.getValue());
            if (sentenceSet == null)
            {
                sentenceSet = new HashSet<>();
                result.put(entry.getValue(), sentenceSet);
            }
            sentenceSet.add(entry.getKey());
        }

        return result;
    }

    /**
     * 从map的值中找出最大值，这个值是从0开始的
     * @param map
     * @return
     */
    private static Double max(Map<String, Double> map)
    {
        Double theMax = 0.0;
        for (Double v : map.values())
        {
            theMax = Math.max(theMax, v);
        }

        return theMax;
    }
}
