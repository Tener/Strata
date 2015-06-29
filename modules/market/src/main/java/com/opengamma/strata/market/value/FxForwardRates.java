/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.market.value;

import java.time.LocalDate;

import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyPair;
import com.opengamma.strata.market.sensitivity.CurveCurrencyParameterSensitivities;
import com.opengamma.strata.market.sensitivity.FxForwardSensitivity;
import com.opengamma.strata.market.sensitivity.PointSensitivityBuilder;

/**
 * Provides access to rates for a currency pair.
 * <p>
 * This provides forward rates for a single {@link Currency pair}, such as 'EUR/GBP'.
 * The forward rate is the conversion rate between two currencies on a fixing date in the future. 
 */
public interface FxForwardRates {

  /**
   * Gets the currency pair.
   * <p>
   * The the currency pair that the forward rates are for.
   * 
   * @return the currency pair
   */
  public abstract CurrencyPair getCurrencyPair();

  /**
   * Gets the valuation date.
   * <p>
   * The raw data in this provider is calibrated for this date.
   * 
   * @return the valuation date
   */
  public abstract LocalDate getValuationDate();

  //-------------------------------------------------------------------------
  /**
   * Gets the forward rate at the specified fixing date.
   * <p>
   * The exchange rate of the currency pair varies over time.
   * This method obtains the estimated rate for the fixing date.
   * <p>
   * This method specifies which of the two currencies in the currency pair is to be treated
   * as the base currency for the purposes of the returned rate.
   * If the specified base currency equals the base currency of the currency pair, then
   * the rate is simply returned. If the specified base currency equals the counter currency
   * of the currency pair, then the inverse rate is returned.
   * As such, an amount in the specified base currency can be directly multiplied by the
   * returned FX rate to perform FX conversion.
   * <p>
   * To convert an amount in the specified base currency to the other currency,
   * multiply it by the returned FX rate.
   * 
   * @param baseCurrency  the base currency that the rate should be expressed against
   * @param referenceDate  the date to query the rate for
   * @return the forward rate of the currency pair
   * @throws RuntimeException if the value cannot be obtained
   */
  public abstract double rate(Currency baseCurrency, LocalDate referenceDate);

  /**
   * Calculates the point sensitivity of the forward rate at the specified date.
   * <p>
   * This returns a sensitivity instance referring to the curve used to determine the forward rate.
   * The sensitivity refers to the result of {@link #rate(Currency, LocalDate)}.
   * 
   * @param baseCurrency  the base currency that the rate should be expressed against
   * @param referenceDate  the date to find the sensitivity for
   * @return the point sensitivity of the rate
   * @throws RuntimeException if the value cannot be obtained
   */
  public abstract PointSensitivityBuilder ratePointSensitivity(Currency baseCurrency, LocalDate referenceDate);

  /**
   * Calculates the sensitivity of the forward rate to the spot rate.
   * <p>
   * The sensitivity refers to the result of {@link #rate(Currency, LocalDate)}.
   * 
   * @param baseCurrency  the base currency that the rate should be expressed against
   * @param referenceDate  the date to find the sensitivity for
   * @return the spot sensitivity of the rate
   * @throws RuntimeException if the value cannot be obtained
   */
  public abstract double rateSpotSensitivity(Currency baseCurrency, LocalDate referenceDate);

  //-------------------------------------------------------------------------
  /**
   * Calculates the curve parameter sensitivity from the point sensitivity.
   * <p>
   * This is used to convert a single point sensitivity to curve parameter sensitivity.
   * 
   * @param pointSensitivity  the point sensitivity to convert
   * @return the parameter sensitivity
   * @throws RuntimeException if the result cannot be calculated
   */
  public abstract CurveCurrencyParameterSensitivities curveParameterSensitivity(FxForwardSensitivity pointSensitivity);

}