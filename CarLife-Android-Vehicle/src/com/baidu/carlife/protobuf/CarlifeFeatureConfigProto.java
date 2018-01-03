// Generated by the protocol buffer compiler.  DO NOT EDIT!

package com.baidu.carlife.protobuf;

public final class CarlifeFeatureConfigProto {
  private CarlifeFeatureConfigProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public static final class CarlifeFeatureConfig extends
      com.google.protobuf.GeneratedMessage {
    // Use CarlifeFeatureConfig.newBuilder() to construct.
    private CarlifeFeatureConfig() {}
    
    private static final CarlifeFeatureConfig defaultInstance = new CarlifeFeatureConfig();
    public static CarlifeFeatureConfig getDefaultInstance() {
      return defaultInstance;
    }
    
    public CarlifeFeatureConfig getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.internal_static_com_baidu_carlife_protobuf_CarlifeFeatureConfig_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.internal_static_com_baidu_carlife_protobuf_CarlifeFeatureConfig_fieldAccessorTable;
    }
    
    // required string key = 1;
    public static final int KEY_FIELD_NUMBER = 1;
    private boolean hasKey;
    private java.lang.String key_ = "";
    public boolean hasKey() { return hasKey; }
    public java.lang.String getKey() { return key_; }
    
    // required int32 value = 2;
    public static final int VALUE_FIELD_NUMBER = 2;
    private boolean hasValue;
    private int value_ = 0;
    public boolean hasValue() { return hasValue; }
    public int getValue() { return value_; }
    
    public final boolean isInitialized() {
      if (!hasKey) return false;
      if (!hasValue) return false;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (hasKey()) {
        output.writeString(1, getKey());
      }
      if (hasValue()) {
        output.writeInt32(2, getValue());
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (hasKey()) {
        size += com.google.protobuf.CodedOutputStream
          .computeStringSize(1, getKey());
      }
      if (hasValue()) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(2, getValue());
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    public static com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeDelimitedFrom(input).buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeDelimitedFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> {
      private com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig result;
      
      // Construct using com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig.newBuilder()
      private Builder() {}
      
      private static Builder create() {
        Builder builder = new Builder();
        builder.result = new com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig();
        return builder;
      }
      
      protected com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig internalGetResult() {
        return result;
      }
      
      public Builder clear() {
        if (result == null) {
          throw new IllegalStateException(
            "Cannot call clear() after build().");
        }
        result = new com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig();
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(result);
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig.getDescriptor();
      }
      
      public com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig getDefaultInstanceForType() {
        return com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig.getDefaultInstance();
      }
      
      public boolean isInitialized() {
        return result.isInitialized();
      }
      public com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig build() {
        if (result != null && !isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return buildPartial();
      }
      
      private com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        if (!isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return buildPartial();
      }
      
      public com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig buildPartial() {
        if (result == null) {
          throw new IllegalStateException(
            "build() has already been called on this Builder.");
        }
        com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig returnMe = result;
        result = null;
        return returnMe;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig) {
          return mergeFrom((com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig other) {
        if (other == com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig.getDefaultInstance()) return this;
        if (other.hasKey()) {
          setKey(other.getKey());
        }
        if (other.hasValue()) {
          setValue(other.getValue());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                return this;
              }
              break;
            }
            case 10: {
              setKey(input.readString());
              break;
            }
            case 16: {
              setValue(input.readInt32());
              break;
            }
          }
        }
      }
      
      
      // required string key = 1;
      public boolean hasKey() {
        return result.hasKey();
      }
      public java.lang.String getKey() {
        return result.getKey();
      }
      public Builder setKey(java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  result.hasKey = true;
        result.key_ = value;
        return this;
      }
      public Builder clearKey() {
        result.hasKey = false;
        result.key_ = getDefaultInstance().getKey();
        return this;
      }
      
      // required int32 value = 2;
      public boolean hasValue() {
        return result.hasValue();
      }
      public int getValue() {
        return result.getValue();
      }
      public Builder setValue(int value) {
        result.hasValue = true;
        result.value_ = value;
        return this;
      }
      public Builder clearValue() {
        result.hasValue = false;
        result.value_ = 0;
        return this;
      }
    }
    
    static {
      com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.getDescriptor();
    }
    
    static {
      com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.internalForceInit();
    }
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_com_baidu_carlife_protobuf_CarlifeFeatureConfig_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_com_baidu_carlife_protobuf_CarlifeFeatureConfig_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\037CarlifeFeatureConfigProto.proto\022\032com.b" +
      "aidu.carlife.protobuf\"2\n\024CarlifeFeatureC" +
      "onfig\022\013\n\003key\030\001 \002(\t\022\r\n\005value\030\002 \002(\005"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_com_baidu_carlife_protobuf_CarlifeFeatureConfig_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_com_baidu_carlife_protobuf_CarlifeFeatureConfig_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_com_baidu_carlife_protobuf_CarlifeFeatureConfig_descriptor,
              new java.lang.String[] { "Key", "Value", },
              com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig.class,
              com.baidu.carlife.protobuf.CarlifeFeatureConfigProto.CarlifeFeatureConfig.Builder.class);
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }
  
  public static void internalForceInit() {}
}
