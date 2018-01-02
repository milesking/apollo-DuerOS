// Generated by the protocol buffer compiler.  DO NOT EDIT!

package com.baidu.carlife.protobuf;

public final class CarlifeCallRecordsListProto {
  private CarlifeCallRecordsListProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public static final class CarlifeCallRecordsList extends
      com.google.protobuf.GeneratedMessage {
    // Use CarlifeCallRecordsList.newBuilder() to construct.
    private CarlifeCallRecordsList() {}
    
    private static final CarlifeCallRecordsList defaultInstance = new CarlifeCallRecordsList();
    public static CarlifeCallRecordsList getDefaultInstance() {
      return defaultInstance;
    }
    
    public CarlifeCallRecordsList getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.internal_static_com_baidu_carlife_protobuf_CarlifeCallRecordsList_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.internal_static_com_baidu_carlife_protobuf_CarlifeCallRecordsList_fieldAccessorTable;
    }
    
    // required int32 cnt = 1;
    public static final int CNT_FIELD_NUMBER = 1;
    private boolean hasCnt;
    private int cnt_ = 0;
    public boolean hasCnt() { return hasCnt; }
    public int getCnt() { return cnt_; }
    
    // repeated .com.baidu.carlife.protobuf.CarlifeCallRecords carlifeCallRecords = 2;
    public static final int CARLIFECALLRECORDS_FIELD_NUMBER = 2;
    private java.util.List<com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords> carlifeCallRecords_ =
      java.util.Collections.emptyList();
    public java.util.List<com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords> getCarlifeCallRecordsList() {
      return carlifeCallRecords_;
    }
    public int getCarlifeCallRecordsCount() { return carlifeCallRecords_.size(); }
    public com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords getCarlifeCallRecords(int index) {
      return carlifeCallRecords_.get(index);
    }
    
    public final boolean isInitialized() {
      if (!hasCnt) return false;
      for (com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords element : getCarlifeCallRecordsList()) {
        if (!element.isInitialized()) return false;
      }
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (hasCnt()) {
        output.writeInt32(1, getCnt());
      }
      for (com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords element : getCarlifeCallRecordsList()) {
        output.writeMessage(2, element);
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (hasCnt()) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(1, getCnt());
      }
      for (com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords element : getCarlifeCallRecordsList()) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(2, element);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    public static com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeDelimitedFrom(input).buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeDelimitedFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> {
      private com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList result;
      
      // Construct using com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList.newBuilder()
      private Builder() {}
      
      private static Builder create() {
        Builder builder = new Builder();
        builder.result = new com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList();
        return builder;
      }
      
      protected com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList internalGetResult() {
        return result;
      }
      
      public Builder clear() {
        if (result == null) {
          throw new IllegalStateException(
            "Cannot call clear() after build().");
        }
        result = new com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList();
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(result);
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList.getDescriptor();
      }
      
      public com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList getDefaultInstanceForType() {
        return com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList.getDefaultInstance();
      }
      
      public boolean isInitialized() {
        return result.isInitialized();
      }
      public com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList build() {
        if (result != null && !isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return buildPartial();
      }
      
      private com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        if (!isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return buildPartial();
      }
      
      public com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList buildPartial() {
        if (result == null) {
          throw new IllegalStateException(
            "build() has already been called on this Builder.");
        }
        if (result.carlifeCallRecords_ != java.util.Collections.EMPTY_LIST) {
          result.carlifeCallRecords_ =
            java.util.Collections.unmodifiableList(result.carlifeCallRecords_);
        }
        com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList returnMe = result;
        result = null;
        return returnMe;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList) {
          return mergeFrom((com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList other) {
        if (other == com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList.getDefaultInstance()) return this;
        if (other.hasCnt()) {
          setCnt(other.getCnt());
        }
        if (!other.carlifeCallRecords_.isEmpty()) {
          if (result.carlifeCallRecords_.isEmpty()) {
            result.carlifeCallRecords_ = new java.util.ArrayList<com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords>();
          }
          result.carlifeCallRecords_.addAll(other.carlifeCallRecords_);
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
            case 8: {
              setCnt(input.readInt32());
              break;
            }
            case 18: {
              com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords.Builder subBuilder = com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords.newBuilder();
              input.readMessage(subBuilder, extensionRegistry);
              addCarlifeCallRecords(subBuilder.buildPartial());
              break;
            }
          }
        }
      }
      
      
      // required int32 cnt = 1;
      public boolean hasCnt() {
        return result.hasCnt();
      }
      public int getCnt() {
        return result.getCnt();
      }
      public Builder setCnt(int value) {
        result.hasCnt = true;
        result.cnt_ = value;
        return this;
      }
      public Builder clearCnt() {
        result.hasCnt = false;
        result.cnt_ = 0;
        return this;
      }
      
      // repeated .com.baidu.carlife.protobuf.CarlifeCallRecords carlifeCallRecords = 2;
      public java.util.List<com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords> getCarlifeCallRecordsList() {
        return java.util.Collections.unmodifiableList(result.carlifeCallRecords_);
      }
      public int getCarlifeCallRecordsCount() {
        return result.getCarlifeCallRecordsCount();
      }
      public com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords getCarlifeCallRecords(int index) {
        return result.getCarlifeCallRecords(index);
      }
      public Builder setCarlifeCallRecords(int index, com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords value) {
        if (value == null) {
          throw new NullPointerException();
        }
        result.carlifeCallRecords_.set(index, value);
        return this;
      }
      public Builder setCarlifeCallRecords(int index, com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords.Builder builderForValue) {
        result.carlifeCallRecords_.set(index, builderForValue.build());
        return this;
      }
      public Builder addCarlifeCallRecords(com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords value) {
        if (value == null) {
          throw new NullPointerException();
        }
        if (result.carlifeCallRecords_.isEmpty()) {
          result.carlifeCallRecords_ = new java.util.ArrayList<com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords>();
        }
        result.carlifeCallRecords_.add(value);
        return this;
      }
      public Builder addCarlifeCallRecords(com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords.Builder builderForValue) {
        if (result.carlifeCallRecords_.isEmpty()) {
          result.carlifeCallRecords_ = new java.util.ArrayList<com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords>();
        }
        result.carlifeCallRecords_.add(builderForValue.build());
        return this;
      }
      public Builder addAllCarlifeCallRecords(
          java.lang.Iterable<? extends com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords> values) {
        if (result.carlifeCallRecords_.isEmpty()) {
          result.carlifeCallRecords_ = new java.util.ArrayList<com.baidu.carlife.protobuf.CarlifeCallRecordsProto.CarlifeCallRecords>();
        }
        super.addAll(values, result.carlifeCallRecords_);
        return this;
      }
      public Builder clearCarlifeCallRecords() {
        result.carlifeCallRecords_ = java.util.Collections.emptyList();
        return this;
      }
    }
    
    static {
      com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.getDescriptor();
    }
    
    static {
      com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.internalForceInit();
    }
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_com_baidu_carlife_protobuf_CarlifeCallRecordsList_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_com_baidu_carlife_protobuf_CarlifeCallRecordsList_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n!CarlifeCallRecordsListProto.proto\022\032com" +
      ".baidu.carlife.protobuf\032\035CarlifeCallReco" +
      "rdsProto.proto\"q\n\026CarlifeCallRecordsList" +
      "\022\013\n\003cnt\030\001 \002(\005\022J\n\022carlifeCallRecords\030\002 \003(" +
      "\0132..com.baidu.carlife.protobuf.CarlifeCa" +
      "llRecords"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_com_baidu_carlife_protobuf_CarlifeCallRecordsList_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_com_baidu_carlife_protobuf_CarlifeCallRecordsList_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_com_baidu_carlife_protobuf_CarlifeCallRecordsList_descriptor,
              new java.lang.String[] { "Cnt", "CarlifeCallRecords", },
              com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList.class,
              com.baidu.carlife.protobuf.CarlifeCallRecordsListProto.CarlifeCallRecordsList.Builder.class);
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.baidu.carlife.protobuf.CarlifeCallRecordsProto.getDescriptor(),
        }, assigner);
  }
  
  public static void internalForceInit() {}
}
