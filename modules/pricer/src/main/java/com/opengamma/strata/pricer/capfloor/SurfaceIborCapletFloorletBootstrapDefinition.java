package com.opengamma.strata.pricer.capfloor;

import java.io.Serializable;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableValidator;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.ImmutableList;
import com.opengamma.strata.basics.date.DayCount;
import com.opengamma.strata.basics.index.IborIndex;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.array.DoubleArray;
import com.opengamma.strata.market.ValueType;
import com.opengamma.strata.market.curve.interpolator.CurveExtrapolators;
import com.opengamma.strata.market.curve.interpolator.CurveInterpolator;
import com.opengamma.strata.market.curve.interpolator.CurveInterpolators;
import com.opengamma.strata.market.option.SimpleStrike;
import com.opengamma.strata.market.surface.SurfaceMetadata;
import com.opengamma.strata.market.surface.Surfaces;
import com.opengamma.strata.market.surface.interpolator.GridSurfaceInterpolator;
import com.opengamma.strata.pricer.common.GenericVolatilitySurfacePeriodParameterMetadata;
import com.opengamma.strata.pricer.option.RawOptionData;

@BeanDefinition(builderScope = "private")
public final class SurfaceIborCapletFloorletBootstrapDefinition
    implements IborCapletFloorletDefinition, ImmutableBean, Serializable {

  /**
   * The name of the volatilities.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final IborCapletFloorletVolatilitiesName name;

  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final IborIndex index;
  /**
   * The day count to use.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final DayCount dayCount;
  /**
   * The interpolator for the caplet volatilities.
   */
  @PropertyDefinition(validate = "notNull")
  private final GridSurfaceInterpolator interpolator;

  //-------------------------------------------------------------------------
  public static SurfaceIborCapletFloorletBootstrapDefinition of(
      IborCapletFloorletVolatilitiesName name,
      IborIndex index,
      DayCount dayCount,
      GridSurfaceInterpolator interpolator) {

    return new SurfaceIborCapletFloorletBootstrapDefinition(name, index, dayCount, interpolator);
  }

  public static SurfaceIborCapletFloorletBootstrapDefinition of(
      IborCapletFloorletVolatilitiesName name,
      IborIndex index,
      DayCount dayCount,
      CurveInterpolator timeInterpolator,
      CurveInterpolator strikeInterpolator) {

    GridSurfaceInterpolator gridInterpolator = GridSurfaceInterpolator.of(
        timeInterpolator,
        CurveExtrapolators.FLAT,
        CurveExtrapolators.LINEAR,
        strikeInterpolator,
        CurveExtrapolators.LINEAR,
        CurveExtrapolators.LINEAR);
    return new SurfaceIborCapletFloorletBootstrapDefinition(name, index, dayCount, gridInterpolator);
  }

  @ImmutableValidator
  private void validate() {
    ArgChecker.isTrue(interpolator.getXExtrapolatorLeft().equals(CurveExtrapolators.FLAT),
        "x extrapolator left must be flat extrapolator");
    ArgChecker.isTrue(interpolator.getXInterpolator().equals(CurveInterpolators.LINEAR) ||
        interpolator.getXInterpolator().equals(CurveInterpolators.STEP_UPPER) ||
        interpolator.getXInterpolator().equals(CurveInterpolators.TIME_SQUARE),
        "x interpolator must be local interpolator");
  }

  //-------------------------------------------------------------------------
  public SurfaceMetadata createMetadata(RawOptionData capFloorData) {
    List<GenericVolatilitySurfacePeriodParameterMetadata> list = new ArrayList<>();
    ImmutableList<Period> expiries = capFloorData.getExpiries();
    int nExpiries = expiries.size();
    DoubleArray strikes = capFloorData.getStrikes();
    int nStrikes = strikes.size();
    for (int i = 0; i < nExpiries; ++i) {
      for (int j = 0; j < nStrikes; ++j) {
        if (Double.isFinite(capFloorData.getData().get(i, j))) {
          list.add(GenericVolatilitySurfacePeriodParameterMetadata.of(expiries.get(i), SimpleStrike.of(strikes.get(j))));
        }
      }
    }
    SurfaceMetadata metadata;
    if (capFloorData.getDataType().equals(ValueType.BLACK_VOLATILITY)) {
      metadata = Surfaces.blackVolatilityByExpiryStrike(name.getName(), dayCount);
    } else if (capFloorData.getDataType().equals(ValueType.NORMAL_VOLATILITY)) {
      metadata = Surfaces.normalVolatilityByExpiryStrike(name.getName(), dayCount);
    } else {
      throw new IllegalArgumentException("Data type not supported");
    }
    return metadata.withParameterMetadata(list);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SurfaceIborCapletFloorletBootstrapDefinition}.
   * @return the meta-bean, not null
   */
  public static SurfaceIborCapletFloorletBootstrapDefinition.Meta meta() {
    return SurfaceIborCapletFloorletBootstrapDefinition.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(SurfaceIborCapletFloorletBootstrapDefinition.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  private SurfaceIborCapletFloorletBootstrapDefinition(
      IborCapletFloorletVolatilitiesName name,
      IborIndex index,
      DayCount dayCount,
      GridSurfaceInterpolator interpolator) {
    JodaBeanUtils.notNull(name, "name");
    JodaBeanUtils.notNull(index, "index");
    JodaBeanUtils.notNull(dayCount, "dayCount");
    JodaBeanUtils.notNull(interpolator, "interpolator");
    this.name = name;
    this.index = index;
    this.dayCount = dayCount;
    this.interpolator = interpolator;
    validate();
  }

  @Override
  public SurfaceIborCapletFloorletBootstrapDefinition.Meta metaBean() {
    return SurfaceIborCapletFloorletBootstrapDefinition.Meta.INSTANCE;
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
   * Gets the index.
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
   * Gets the interpolator for the caplet volatilities.
   * @return the value of the property, not null
   */
  public GridSurfaceInterpolator getInterpolator() {
    return interpolator;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SurfaceIborCapletFloorletBootstrapDefinition other = (SurfaceIborCapletFloorletBootstrapDefinition) obj;
      return JodaBeanUtils.equal(name, other.name) &&
          JodaBeanUtils.equal(index, other.index) &&
          JodaBeanUtils.equal(dayCount, other.dayCount) &&
          JodaBeanUtils.equal(interpolator, other.interpolator);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(name);
    hash = hash * 31 + JodaBeanUtils.hashCode(index);
    hash = hash * 31 + JodaBeanUtils.hashCode(dayCount);
    hash = hash * 31 + JodaBeanUtils.hashCode(interpolator);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("SurfaceIborCapletFloorletBootstrapDefinition{");
    buf.append("name").append('=').append(name).append(',').append(' ');
    buf.append("index").append('=').append(index).append(',').append(' ');
    buf.append("dayCount").append('=').append(dayCount).append(',').append(' ');
    buf.append("interpolator").append('=').append(JodaBeanUtils.toString(interpolator));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SurfaceIborCapletFloorletBootstrapDefinition}.
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
        this, "name", SurfaceIborCapletFloorletBootstrapDefinition.class, IborCapletFloorletVolatilitiesName.class);
    /**
     * The meta-property for the {@code index} property.
     */
    private final MetaProperty<IborIndex> index = DirectMetaProperty.ofImmutable(
        this, "index", SurfaceIborCapletFloorletBootstrapDefinition.class, IborIndex.class);
    /**
     * The meta-property for the {@code dayCount} property.
     */
    private final MetaProperty<DayCount> dayCount = DirectMetaProperty.ofImmutable(
        this, "dayCount", SurfaceIborCapletFloorletBootstrapDefinition.class, DayCount.class);
    /**
     * The meta-property for the {@code interpolator} property.
     */
    private final MetaProperty<GridSurfaceInterpolator> interpolator = DirectMetaProperty.ofImmutable(
        this, "interpolator", SurfaceIborCapletFloorletBootstrapDefinition.class, GridSurfaceInterpolator.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "name",
        "index",
        "dayCount",
        "interpolator");

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
        case 2096253127:  // interpolator
          return interpolator;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SurfaceIborCapletFloorletBootstrapDefinition> builder() {
      return new SurfaceIborCapletFloorletBootstrapDefinition.Builder();
    }

    @Override
    public Class<? extends SurfaceIborCapletFloorletBootstrapDefinition> beanType() {
      return SurfaceIborCapletFloorletBootstrapDefinition.class;
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
     * The meta-property for the {@code interpolator} property.
     * @return the meta-property, not null
     */
    public MetaProperty<GridSurfaceInterpolator> interpolator() {
      return interpolator;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3373707:  // name
          return ((SurfaceIborCapletFloorletBootstrapDefinition) bean).getName();
        case 100346066:  // index
          return ((SurfaceIborCapletFloorletBootstrapDefinition) bean).getIndex();
        case 1905311443:  // dayCount
          return ((SurfaceIborCapletFloorletBootstrapDefinition) bean).getDayCount();
        case 2096253127:  // interpolator
          return ((SurfaceIborCapletFloorletBootstrapDefinition) bean).getInterpolator();
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
   * The bean-builder for {@code SurfaceIborCapletFloorletBootstrapDefinition}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<SurfaceIborCapletFloorletBootstrapDefinition> {

    private IborCapletFloorletVolatilitiesName name;
    private IborIndex index;
    private DayCount dayCount;
    private GridSurfaceInterpolator interpolator;

    /**
     * Restricted constructor.
     */
    private Builder() {
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
        case 2096253127:  // interpolator
          return interpolator;
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
        case 2096253127:  // interpolator
          this.interpolator = (GridSurfaceInterpolator) newValue;
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
    public SurfaceIborCapletFloorletBootstrapDefinition build() {
      return new SurfaceIborCapletFloorletBootstrapDefinition(
          name,
          index,
          dayCount,
          interpolator);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(160);
      buf.append("SurfaceIborCapletFloorletBootstrapDefinition.Builder{");
      buf.append("name").append('=').append(JodaBeanUtils.toString(name)).append(',').append(' ');
      buf.append("index").append('=').append(JodaBeanUtils.toString(index)).append(',').append(' ');
      buf.append("dayCount").append('=').append(JodaBeanUtils.toString(dayCount)).append(',').append(' ');
      buf.append("interpolator").append('=').append(JodaBeanUtils.toString(interpolator));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}