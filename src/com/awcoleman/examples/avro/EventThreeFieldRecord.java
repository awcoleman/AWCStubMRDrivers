/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package com.awcoleman.examples.avro;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class EventThreeFieldRecord extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"EventThreeFieldRecord\",\"namespace\":\"com.awcoleman.examples.avro\",\"fields\":[{\"name\":\"uuid\",\"type\":\"long\"},{\"name\":\"datetime\",\"type\":\"string\"},{\"name\":\"category\",\"type\":\"int\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public long uuid;
  @Deprecated public java.lang.CharSequence datetime;
  @Deprecated public int category;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public EventThreeFieldRecord() {}

  /**
   * All-args constructor.
   */
  public EventThreeFieldRecord(java.lang.Long uuid, java.lang.CharSequence datetime, java.lang.Integer category) {
    this.uuid = uuid;
    this.datetime = datetime;
    this.category = category;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return uuid;
    case 1: return datetime;
    case 2: return category;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: uuid = (java.lang.Long)value$; break;
    case 1: datetime = (java.lang.CharSequence)value$; break;
    case 2: category = (java.lang.Integer)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'uuid' field.
   */
  public java.lang.Long getUuid() {
    return uuid;
  }

  /**
   * Sets the value of the 'uuid' field.
   * @param value the value to set.
   */
  public void setUuid(java.lang.Long value) {
    this.uuid = value;
  }

  /**
   * Gets the value of the 'datetime' field.
   */
  public java.lang.CharSequence getDatetime() {
    return datetime;
  }

  /**
   * Sets the value of the 'datetime' field.
   * @param value the value to set.
   */
  public void setDatetime(java.lang.CharSequence value) {
    this.datetime = value;
  }

  /**
   * Gets the value of the 'category' field.
   */
  public java.lang.Integer getCategory() {
    return category;
  }

  /**
   * Sets the value of the 'category' field.
   * @param value the value to set.
   */
  public void setCategory(java.lang.Integer value) {
    this.category = value;
  }

  /**
   * Creates a new EventThreeFieldRecord RecordBuilder.
   * @return A new EventThreeFieldRecord RecordBuilder
   */
  public static com.awcoleman.examples.avro.EventThreeFieldRecord.Builder newBuilder() {
    return new com.awcoleman.examples.avro.EventThreeFieldRecord.Builder();
  }
  
  /**
   * Creates a new EventThreeFieldRecord RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new EventThreeFieldRecord RecordBuilder
   */
  public static com.awcoleman.examples.avro.EventThreeFieldRecord.Builder newBuilder(com.awcoleman.examples.avro.EventThreeFieldRecord.Builder other) {
    return new com.awcoleman.examples.avro.EventThreeFieldRecord.Builder(other);
  }
  
  /**
   * Creates a new EventThreeFieldRecord RecordBuilder by copying an existing EventThreeFieldRecord instance.
   * @param other The existing instance to copy.
   * @return A new EventThreeFieldRecord RecordBuilder
   */
  public static com.awcoleman.examples.avro.EventThreeFieldRecord.Builder newBuilder(com.awcoleman.examples.avro.EventThreeFieldRecord other) {
    return new com.awcoleman.examples.avro.EventThreeFieldRecord.Builder(other);
  }
  
  /**
   * RecordBuilder for EventThreeFieldRecord instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<EventThreeFieldRecord>
    implements org.apache.avro.data.RecordBuilder<EventThreeFieldRecord> {

    private long uuid;
    private java.lang.CharSequence datetime;
    private int category;

    /** Creates a new Builder */
    private Builder() {
      super(com.awcoleman.examples.avro.EventThreeFieldRecord.SCHEMA$);
    }
    
    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.awcoleman.examples.avro.EventThreeFieldRecord.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.uuid)) {
        this.uuid = data().deepCopy(fields()[0].schema(), other.uuid);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.datetime)) {
        this.datetime = data().deepCopy(fields()[1].schema(), other.datetime);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.category)) {
        this.category = data().deepCopy(fields()[2].schema(), other.category);
        fieldSetFlags()[2] = true;
      }
    }
    
    /**
     * Creates a Builder by copying an existing EventThreeFieldRecord instance
     * @param other The existing instance to copy.
     */
    private Builder(com.awcoleman.examples.avro.EventThreeFieldRecord other) {
            super(com.awcoleman.examples.avro.EventThreeFieldRecord.SCHEMA$);
      if (isValidValue(fields()[0], other.uuid)) {
        this.uuid = data().deepCopy(fields()[0].schema(), other.uuid);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.datetime)) {
        this.datetime = data().deepCopy(fields()[1].schema(), other.datetime);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.category)) {
        this.category = data().deepCopy(fields()[2].schema(), other.category);
        fieldSetFlags()[2] = true;
      }
    }

    /**
      * Gets the value of the 'uuid' field.
      * @return The value.
      */
    public java.lang.Long getUuid() {
      return uuid;
    }

    /**
      * Sets the value of the 'uuid' field.
      * @param value The value of 'uuid'.
      * @return This builder.
      */
    public com.awcoleman.examples.avro.EventThreeFieldRecord.Builder setUuid(long value) {
      validate(fields()[0], value);
      this.uuid = value;
      fieldSetFlags()[0] = true;
      return this; 
    }

    /**
      * Checks whether the 'uuid' field has been set.
      * @return True if the 'uuid' field has been set, false otherwise.
      */
    public boolean hasUuid() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'uuid' field.
      * @return This builder.
      */
    public com.awcoleman.examples.avro.EventThreeFieldRecord.Builder clearUuid() {
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'datetime' field.
      * @return The value.
      */
    public java.lang.CharSequence getDatetime() {
      return datetime;
    }

    /**
      * Sets the value of the 'datetime' field.
      * @param value The value of 'datetime'.
      * @return This builder.
      */
    public com.awcoleman.examples.avro.EventThreeFieldRecord.Builder setDatetime(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.datetime = value;
      fieldSetFlags()[1] = true;
      return this; 
    }

    /**
      * Checks whether the 'datetime' field has been set.
      * @return True if the 'datetime' field has been set, false otherwise.
      */
    public boolean hasDatetime() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'datetime' field.
      * @return This builder.
      */
    public com.awcoleman.examples.avro.EventThreeFieldRecord.Builder clearDatetime() {
      datetime = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'category' field.
      * @return The value.
      */
    public java.lang.Integer getCategory() {
      return category;
    }

    /**
      * Sets the value of the 'category' field.
      * @param value The value of 'category'.
      * @return This builder.
      */
    public com.awcoleman.examples.avro.EventThreeFieldRecord.Builder setCategory(int value) {
      validate(fields()[2], value);
      this.category = value;
      fieldSetFlags()[2] = true;
      return this; 
    }

    /**
      * Checks whether the 'category' field has been set.
      * @return True if the 'category' field has been set, false otherwise.
      */
    public boolean hasCategory() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'category' field.
      * @return This builder.
      */
    public com.awcoleman.examples.avro.EventThreeFieldRecord.Builder clearCategory() {
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    public EventThreeFieldRecord build() {
      try {
        EventThreeFieldRecord record = new EventThreeFieldRecord();
        record.uuid = fieldSetFlags()[0] ? this.uuid : (java.lang.Long) defaultValue(fields()[0]);
        record.datetime = fieldSetFlags()[1] ? this.datetime : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.category = fieldSetFlags()[2] ? this.category : (java.lang.Integer) defaultValue(fields()[2]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}