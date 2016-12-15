/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.capfloor;

import static com.opengamma.strata.market.ValueType.BLACK_VOLATILITY;
import static com.opengamma.strata.market.ValueType.NORMAL_VOLATILITY;

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.strata.basics.date.DayCount;
import com.opengamma.strata.basics.index.IborIndex;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.array.DoubleArray;
import com.opengamma.strata.collect.array.DoubleMatrix;
import com.opengamma.strata.market.curve.ConstantCurve;
import com.opengamma.strata.market.curve.Curve;
import com.opengamma.strata.market.surface.SurfaceMetadata;
import com.opengamma.strata.market.surface.Surfaces;
import com.opengamma.strata.market.surface.interpolator.GridSurfaceInterpolator;
import com.opengamma.strata.math.impl.interpolation.PenaltyMatrixGenerator;
import com.opengamma.strata.pricer.option.RawOptionData;

/**
 * Definition of caplet volatilities calibration.
 * <p>
 * This definition is used with {@link DirectIborCapletFloorletVolatilityCalibrator}. 
 * The volatilities of the constituent caplets in the market caps are "model parameters" 
 * and calibrated to the market data under a certain penalty constraint.
 * The resulting volatilities object will be a set of caplet volatilities interpolated by {@link GridSurfaceInterpolator}.
 */
@BeanDefinition
public final class DirectIborCapletFloorletDefinition
    implements IborCapletFloorletDefinition, ImmutableBean, Serializable {

  /**
   * The name of the volatilities.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final IborCapletFloorletVolatilitiesName name;
  /**
   * The Ibor index for which the data is valid.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final IborIndex index;
  /**
   * The day count to use.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final DayCount dayCount;
  /**
   * Penalty intensity parameter for expiry dimension.
   */
  @PropertyDefinition(validate = "ArgChecker.notNegative")
  private final double lambdaExpiry;
  /**
   * Penalty intensity parameter for strike dimension.
   */
  @PropertyDefinition(validate = "ArgChecker.notNegative")
  private final double lambdaStrike;
  /**
   * The interpolator for the caplet volatilities.
   */
  @PropertyDefinition(validate = "notNull")
  private final GridSurfaceInterpolator interpolator;
  /**
   * The shift parameter of shifted Black model.
   * <p>
   * The x value of the curve is the expiry.
   * The market volatilities are calibrated to shifted Black model if this field is not null.
   */
  @PropertyDefinition(get = "optional")
  private final Curve shiftCurve;

  //-------------------------------------------------------------------------
  public static DirectIborCapletFloorletDefinition of(
      IborCapletFloorletVolatilitiesName name,
      IborIndex index,
      DayCount dayCount,
      double lambdaExpiry,
      double lambdaStrike,
      GridSurfaceInterpolator interpolator) {

    return new DirectIborCapletFloorletDefinition(name, index, dayCount, lambdaExpiry, lambdaStrike, interpolator, null);
  }

  public static DirectIborCapletFloorletDefinition of(
      IborCapletFloorletVolatilitiesName name,
      IborIndex index,
      DayCount dayCount,
      double lambdaExpiry,
      double lambdaStrike,
      GridSurfaceInterpolator interpolator,
      double shift) {

    ConstantCurve shiftCurve = ConstantCurve.of("Shift curve", shift);
    return new DirectIborCapletFloorletDefinition(name, index, dayCount, lambdaExpiry, lambdaStrike, interpolator, shiftCurve);
  }

  //-------------------------------------------------------------------------
  @Override
  public SurfaceMetadata createMetadata(RawOptionData capFloorData) {
    SurfaceMetadata metadata;
    if (capFloorData.getDataType().equals(BLACK_VOLATILITY)) {
      metadata = Surfaces.blackVolatilityByExpiryStrike(name.getName(), dayCount);
    } else if (capFloorData.getDataType().equals(NORMAL_VOLATILITY)) {
      metadata = Surfaces.normalVolatilityByExpiryStrike(name.getName(), dayCount);
    } else {
      throw new IllegalArgumentException("Data type not supported");
    }
    return metadata;
  }


  public DoubleMatrix computePenaltyMatrix(DoubleArray strikes, DoubleArray expiries) {
    // use second order difference unless too few points
    int diffOrderK = Math.min(2, strikes.size() - 1);
    int diffOrderT = Math.min(2, expiries.size() - 1);
    double effLambdaK = diffOrderK == 0 ? 0.0 : lambdaStrike;
    double effLambdaT = diffOrderT == 0 ? 0.0 : lambdaExpiry;
    return PenaltyMatrixGenerator.getPenaltyMatrix(
        new double[][] {expiries.toArray(), strikes.toArray()},
        new int[] {diffOrderT, diffOrderK},
        new double[] {effLambdaT, effLambdaK});
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DirectIborCapletFloorletDefinition}.
   * @return the meta-bean, not null
   */
  public static DirectIborCapletFloorletDefinition.Meta meta() {
    return DirectIborCapletFloorletDefinition.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DirectIborCapletFloorletDefinition.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static DirectIborCapletFloorletDefinition.Builder builder() {
    return new DirectIborCapletFloorletDefinition.Builder();
  }

  private DirectIborCapletFloorletDefinition(
      IborCapletFloorletVolatilitiesName name,
      IborIndex index,
      DayCount dayCount,
      double lambdaExpiry,
      double lambdaStrike,
      GridSurfaceInterpolator interpolator,
      Curve shiftCurve) {
    JodaBeanUtils.notNull(name, "name");
    JodaBeanUtils.notNull(index, "index");
    JodaBeanUtils.notNull(dayCount, "dayCount");
    ArgChecker.notNegative(lambdaExpiry, "lambdaExpiry");
    ArgChecker.notNegative(lambdaStrike, "lambdaStrike");
    JodaBeanUtils.notNull(interpolator, "interpolator");
    this.name = name;
    this.index = index;
    this.dayCount = dayCount;
    this.lambdaExpiry = lambdaExpiry;
    this.lambdaStrike = lambdaStrike;
    this.interpolator = interpolator;
    this.shiftCurve = shiftCurve;
  }

  @Override
  public DirectIborCapletFloorletDefinition.Meta metaBean() {
    return DirectIborCapletFloorletDefinition.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name of the volatilities.
   * @return the value of the property, not null
   */
  @Override
  public IborCapletFloorletVolatilitiesName getName() {
    return name;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Ibor index for which the data is valid.
   * @return the value of the property, not null
   */
  @Override
  public IborIndex getIndex() {
    return index;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the day count to use.
   * @return the value of the property, not null
   */
  @Override
  public DayCount getDayCount() {
    return dayCount;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets penalty intensity parameter for expiry dimension.
   * @return the value of the property
   */
  public double getLambdaExpiry() {
    return lambdaExpiry;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets penalty intensity parameter for strike dimension.
   * @return the value of the property
   */
  public double getLambdaStrike() {
    return lambdaStrike;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the interpolator for the caplet volatilities.
   * @return the value of the property, not null
   */
  public GridSurfaceInterpolator getInterpolator() {
    return interpolator;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the shift parameter of shifted Black model.
   * <p>
   * The x value of the curve is the expiry.
   * The market volatilities are calibrated to shifted Black model if this field is not null.
   * @return the optional value of the property, not null
   */
  public Optional<Curve> getShiftCurve() {
    return Optional.ofNullable(shiftCurve);
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      DirectIborCapletFloorletDefinition other = (DirectIborCapletFloorletDefinition) obj;
      return JodaBeanUtils.equal(name, other.name) &&
          JodaBeanUtils.equal(index, other.index) &&
          JodaBeanUtils.equal(dayCount, other.dayCount) &&
          JodaBeanUtils.equal(lambdaExpiry, other.lambdaExpiry) &&
          JodaBeanUtils.equal(lambdaStrike, other.lambdaStrike) &&
          JodaBeanUtils.equal(interpolator, other.interpolator) &&
          JodaBeanUtils.equal(shiftCurve, other.shiftCurve);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(name);
    hash = hash * 31 + JodaBeanUtils.hashCode(index);
    hash = hash * 31 + JodaBeanUtils.hashCode(dayCount);
    hash = hash * 31 + JodaBeanUtils.hashCode(lambdaExpiry);
    hash = hash * 31 + JodaBeanUtils.hashCode(lambdaStrike);
    hash = hash * 31 + JodaBeanUtils.hashCode(interpolator);
    hash = hash * 31 + JodaBeanUtils.hashCode(shiftCurve);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("DirectIborCapletFloorletDefinition{");
    buf.append("name").append('=').append(name).append(',').append(' ');
    buf.append("index").append('=').append(index).append(',').append(' ');
    buf.append("dayCount").append('=').append(dayCount).append(',').append(' ');
    buf.append("lambdaExpiry").append('=').append(lambdaExpiry).append(',').append(' ');
    buf.append("lambdaStrike").append('=').append(lambdaStrike).append(',').append(' ');
    buf.append("interpolator").append('=').append(interpolator).append(',').append(' ');
    buf.append("shiftCurve").append('=').append(JodaBeanUtils.toString(shiftCurve));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DirectIborCapletFloorletDefinition}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<IborCapletFloorletVolatilitiesName> name = DirectMetaProperty.ofImmutable(
        this, "name", DirectIborCapletFloorletDefinition.class, IborCapletFloorletVolatilitiesName.class);
    /**
     * The meta-property for the {@code index} property.
     */
    private final MetaProperty<IborIndex> index = DirectMetaProperty.ofImmutable(
        this, "index", DirectIborCapletFloorletDefinition.class, IborIndex.class);
    /**
     * The meta-property for the {@code dayCount} property.
     */
    private final MetaProperty<DayCount> dayCount = DirectMetaProperty.ofImmutable(
        this, "dayCount", DirectIborCapletFloorletDefinition.class, DayCount.class);
    /**
     * The meta-property for the {@code lambdaExpiry} property.
     */
    private final MetaProperty<Double> lambdaExpiry = DirectMetaProperty.ofImmutable(
        this, "lambdaExpiry", DirectIborCapletFloorletDefinition.class, Double.TYPE);
    /**
     * The meta-property for the {@code lambdaStrike} property.
     */
    private final MetaProperty<Double> lambdaStrike = DirectMetaProperty.ofImmutable(
        this, "lambdaStrike", DirectIborCapletFloorletDefinition.class, Double.TYPE);
    /**
     * The meta-property for the {@code interpolator} property.
     */
    private final MetaProperty<GridSurfaceInterpolator> interpolator = DirectMetaProperty.ofImmutable(
        this, "interpolator", DirectIborCapletFloorletDefinition.class, GridSurfaceInterpolator.class);
    /**
     * The meta-property for the {@code shiftCurve} property.
     */
    private final MetaProperty<Curve> shiftCurve = DirectMetaProperty.ofImmutable(
        this, "shiftCurve", DirectIborCapletFloorletDefinition.class, Curve.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "name",
        "index",
        "dayCount",
        "lambdaExpiry",
        "lambdaStrike",
        "interpolator",
        "shiftCurve");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3373707:  // name
          return name;
        case 100346066:  // index
          return index;
        case 1905311443:  // dayCount
          return dayCount;
        case -1966011430:  // lambdaExpiry
          return lambdaExpiry;
        case -1568838055:  // lambdaStrike
          return lambdaStrike;
        case 2096253127:  // interpolator
          return interpolator;
        case 1908090253:  // shiftCurve
          return shiftCurve;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public DirectIborCapletFloorletDefinition.Builder builder() {
      return new DirectIborCapletFloorletDefinition.Builder();
    }

    @Override
    public Class<? extends DirectIborCapletFloorletDefinition> beanType() {
      return DirectIborCapletFloorletDefinition.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public MetaProperty<IborCapletFloorletVolatilitiesName> name() {
      return name;
    }

    /**
     * The meta-property for the {@code index} property.
     * @return the meta-property, not null
     */
    public MetaProperty<IborIndex> index() {
      return index;
    }

    /**
     * The meta-property for the {@code dayCount} property.
     * @return the meta-property, not null
     */
    public MetaProperty<DayCount> dayCount() {
      return dayCount;
    }

    /**
     * The meta-property for the {@code lambdaExpiry} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> lambdaExpiry() {
      return lambdaExpiry;
    }

    /**
     * The meta-property for the {@code lambdaStrike} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> lambdaStrike() {
      return lambdaStrike;
    }

    /**
     * The meta-property for the {@code interpolator} property.
     * @return the meta-property, not null
     */
    public MetaProperty<GridSurfaceInterpolator> interpolator() {
      return interpolator;
    }

    /**
     * The meta-property for the {@code shiftCurve} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Curve> shiftCurve() {
      return shiftCurve;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3373707:  // name
          return ((DirectIborCapletFloorletDefinition) bean).getName();
        case 100346066:  // index
          return ((DirectIborCapletFloorletDefinition) bean).getIndex();
        case 1905311443:  // dayCount
          return ((DirectIborCapletFloorletDefinition) bean).getDayCount();
        case -1966011430:  // lambdaExpiry
          return ((DirectIborCapletFloorletDefinition) bean).getLambdaExpiry();
        case -1568838055:  // lambdaStrike
          return ((DirectIborCapletFloorletDefinition) bean).getLambdaStrike();
        case 2096253127:  // interpolator
          return ((DirectIborCapletFloorletDefinition) bean).getInterpolator();
        case 1908090253:  // shiftCurve
          return ((DirectIborCapletFloorletDefinition) bean).shiftCurve;
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code DirectIborCapletFloorletDefinition}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<DirectIborCapletFloorletDefinition> {

    private IborCapletFloorletVolatilitiesName name;
    private IborIndex index;
    private DayCount dayCount;
    private double lambdaExpiry;
    private double lambdaStrike;
    private GridSurfaceInterpolator interpolator;
    private Curve shiftCurve;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(DirectIborCapletFloorletDefinition beanToCopy) {
      this.name = beanToCopy.getName();
      this.index = beanToCopy.getIndex();
      this.dayCount = beanToCopy.getDayCount();
      this.lambdaExpiry = beanToCopy.getLambdaExpiry();
      this.lambdaStrike = beanToCopy.getLambdaStrike();
      this.interpolator = beanToCopy.getInterpolator();
      this.shiftCurve = beanToCopy.shiftCurve;
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3373707:  // name
          return name;
        case 100346066:  // index
          return index;
        case 1905311443:  // dayCount
          return dayCount;
        case -1966011430:  // lambdaExpiry
          return lambdaExpiry;
        case -1568838055:  // lambdaStrike
          return lambdaStrike;
        case 2096253127:  // interpolator
          return interpolator;
        case 1908090253:  // shiftCurve
          return shiftCurve;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 3373707:  // name
          this.name = (IborCapletFloorletVolatilitiesName) newValue;
          break;
        case 100346066:  // index
          this.index = (IborIndex) newValue;
          break;
        case 1905311443:  // dayCount
          this.dayCount = (DayCount) newValue;
          break;
        case -1966011430:  // lambdaExpiry
          this.lambdaExpiry = (Double) newValue;
          break;
        case -1568838055:  // lambdaStrike
          this.lambdaStrike = (Double) newValue;
          break;
        case 2096253127:  // interpolator
          this.interpolator = (GridSurfaceInterpolator) newValue;
          break;
        case 1908090253:  // shiftCurve
          this.shiftCurve = (Curve) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public DirectIborCapletFloorletDefinition build() {
      return new DirectIborCapletFloorletDefinition(
          name,
          index,
          dayCount,
          lambdaExpiry,
          lambdaStrike,
          interpolator,
          shiftCurve);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the name of the volatilities.
     * @param name  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder name(IborCapletFloorletVolatilitiesName name) {
      JodaBeanUtils.notNull(name, "name");
      this.name = name;
      return this;
    }

    /**
     * Sets the Ibor index for which the data is valid.
     * @param index  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder index(IborIndex index) {
      JodaBeanUtils.notNull(index, "index");
      this.index = index;
      return this;
    }

    /**
     * Sets the day count to use.
     * @param dayCount  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder dayCount(DayCount dayCount) {
      JodaBeanUtils.notNull(dayCount, "dayCount");
      this.dayCount = dayCount;
      return this;
    }

    /**
     * Sets penalty intensity parameter for expiry dimension.
     * @param lambdaExpiry  the new value
     * @return this, for chaining, not null
     */
    public Builder lambdaExpiry(double lambdaExpiry) {
      ArgChecker.notNegative(lambdaExpiry, "lambdaExpiry");
      this.lambdaExpiry = lambdaExpiry;
      return this;
    }

    /**
     * Sets penalty intensity parameter for strike dimension.
     * @param lambdaStrike  the new value
     * @return this, for chaining, not null
     */
    public Builder lambdaStrike(double lambdaStrike) {
      ArgChecker.notNegative(lambdaStrike, "lambdaStrike");
      this.lambdaStrike = lambdaStrike;
      return this;
    }

    /**
     * Sets the interpolator for the caplet volatilities.
     * @param interpolator  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder interpolator(GridSurfaceInterpolator interpolator) {
      JodaBeanUtils.notNull(interpolator, "interpolator");
      this.interpolator = interpolator;
      return this;
    }

    /**
     * Sets the shift parameter of shifted Black model.
     * <p>
     * The x value of the curve is the expiry.
     * The market volatilities are calibrated to shifted Black model if this field is not null.
     * @param shiftCurve  the new value
     * @return this, for chaining, not null
     */
    public Builder shiftCurve(Curve shiftCurve) {
      this.shiftCurve = shiftCurve;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(256);
      buf.append("DirectIborCapletFloorletDefinition.Builder{");
      buf.append("name").append('=').append(JodaBeanUtils.toString(name)).append(',').append(' ');
      buf.append("index").append('=').append(JodaBeanUtils.toString(index)).append(',').append(' ');
      buf.append("dayCount").append('=').append(JodaBeanUtils.toString(dayCount)).append(',').append(' ');
      buf.append("lambdaExpiry").append('=').append(JodaBeanUtils.toString(lambdaExpiry)).append(',').append(' ');
      buf.append("lambdaStrike").append('=').append(JodaBeanUtils.toString(lambdaStrike)).append(',').append(' ');
      buf.append("interpolator").append('=').append(JodaBeanUtils.toString(interpolator)).append(',').append(' ');
      buf.append("shiftCurve").append('=').append(JodaBeanUtils.toString(shiftCurve));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}