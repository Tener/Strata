/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.function.calculation.future;

import static com.opengamma.strata.engine.calculations.function.FunctionUtils.toScenarioResult;

import java.util.Optional;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.engine.calculations.DefaultSingleCalculationMarketData;
import com.opengamma.strata.engine.calculations.function.result.ScenarioResult;
import com.opengamma.strata.engine.marketdata.CalculationMarketData;
import com.opengamma.strata.engine.marketdata.CalculationRequirements;
import com.opengamma.strata.engine.marketdata.SingleCalculationMarketData;
import com.opengamma.strata.finance.future.GenericFutureTrade;
import com.opengamma.strata.function.calculation.AbstractCalculationFunction;
import com.opengamma.strata.market.key.QuoteKey;

/**
 * Calculates a result of a {@code GenericFutureTrade} for each of a set of scenarios.
 * 
 * @param <T>  the return type
 */
public abstract class AbstractGenericFutureFunction<T>
    extends AbstractCalculationFunction<GenericFutureTrade, ScenarioResult<T>> {

  /**
   * Creates a new instance which will return results from the {@code execute} method that support automatic
   * currency conversion if the underlying results support it.
   */
  protected AbstractGenericFutureFunction() {
    super();
  }

  /**
   * Creates a new instance.
   *
   * @param convertCurrencies if this is true the value returned by the {@code execute} method will support
   *   automatic currency conversion if the underlying results support it
   */
  protected AbstractGenericFutureFunction(boolean convertCurrencies) {
    super(convertCurrencies);
  }

  //-------------------------------------------------------------------------
  @Override
  public CalculationRequirements requirements(GenericFutureTrade trade) {
    QuoteKey key = QuoteKey.of(trade.getSecurity().getStandardId());
    return CalculationRequirements.builder()
        .singleValueRequirements(ImmutableSet.of(key))
        .outputCurrencies(trade.getProduct().getCurrency())
        .build();
  }

  @Override
  public ScenarioResult<T> execute(GenericFutureTrade trade, CalculationMarketData marketData) {
    return IntStream.range(0, marketData.getScenarioCount())
        .mapToObj(index -> new DefaultSingleCalculationMarketData(marketData, index))
        .map(md -> execute(trade, md))
        .collect(toScenarioResult(isConvertCurrencies()));
  }

  @Override
  public Optional<Currency> defaultReportingCurrency(GenericFutureTrade target) {
    return Optional.of(target.getProduct().getCurrency());
  }

  // execute for a single trade
  protected abstract T execute(GenericFutureTrade trade, SingleCalculationMarketData marketData);

}