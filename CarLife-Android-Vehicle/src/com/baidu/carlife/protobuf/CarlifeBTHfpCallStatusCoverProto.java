// Generated by the protocol buffer compiler.  DO NOT EDIT!

package com.baidu.carlife.protobuf;

public final class CarlifeBTHfpCallStatusCoverProto {
  private CarlifeBTHfpCallStatusCoverProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public static final class CarlifeBTHfpCallStatusCover extends
      com.google.protobuf.GeneratedMessage {
    // Use CarlifeBTHfpCallStatusCover.newBuilder() to construct.
    private CarlifeBTHfpCallStatusCover() {}
    
    private static final CarlifeBTHfpCallStatusCover defaultInstance = new CarlifeBTHfpCallStatusCover();
    public static CarlifeBTHfpCallStatusCover getDefaultInstance() {
      return defaultInstance;
    }
    
    public CarlifeBTHfpCallStatusCover getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.internal_static_com_baidu_carlife_protobuf_CarlifeBTHfpCallStatusCover_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.internal_static_com_baidu_carlife_protobuf_CarlifeBTHfpCallStatusCover_fieldAccessorTable;
    }
    
    // required int32 state = 1;
    public static final int STATE_FIELD_NUMBER = 1;
    private boolean hasState;
    private int state_ = 0;
    public boolean hasState() { return hasState; }
    public int getState() { return state_; }
    
    // required string phoneNum = 2;
    public static final int PHONENUM_FIELD_NUMBER = 2;
    private boolean hasPhoneNum;
    private java.lang.String phoneNum_ = "";
    public boolean hasPhoneNum() { return hasPhoneNum; }
    public java.lang.String getPhoneNum() { return phoneNum_; }
    
    // required string name = 3;
    public static final int NAME_FIELD_NUMBER = 3;
    private boolean hasName;
    private java.lang.String name_ = "";
    public boolean hasName() { return hasName; }
    public java.lang.String getName() { return name_; }
    
    public final boolean isInitialized() {
      if (!hasState) return false;
      if (!hasPhoneNum) return false;
      if (!hasName) return false;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (hasState()) {
        output.writeInt32(1, getState());
      }
      if (hasPhoneNum()) {
        output.writeString(2, getPhoneNum());
      }
      if (hasName()) {
        output.writeString(3, getName());
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (hasState()) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(1, getState());
      }
      if (hasPhoneNum()) {
        size += com.google.protobuf.CodedOutputStream
          .computeStringSize(2, getPhoneNum());
      }
      if (hasName()) {
        size += com.google.protobuf.CodedOutputStream
          .computeStringSize(3, getName());
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    public static com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeDelimitedFrom(input).buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeDelimitedFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> {
      private com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover result;
      
      // Construct using com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover.newBuilder()
      private Builder() {}
      
      private static Builder create() {
        Builder builder = new Builder();
        builder.result = new com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover();
        return builder;
      }
      
      protected com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover internalGetResult() {
        return result;
      }
      
      public Builder clear() {
        if (result == null) {
          throw new IllegalStateException(
            "Cannot call clear() after build().");
        }
        result = new com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover();
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(result);
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover.getDescriptor();
      }
      
      public com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover getDefaultInstanceForType() {
        return com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover.getDefaultInstance();
      }
      
      public boolean isInitialized() {
        return result.isInitialized();
      }
      public com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover build() {
        if (result != null && !isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return buildPartial();
      }
      
      private com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        if (!isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return buildPartial();
      }
      
      public com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover buildPartial() {
        if (result == null) {
          throw new IllegalStateException(
            "build() has already been called on this Builder.");
        }
        com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover returnMe = result;
        result = null;
        return returnMe;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover) {
          return mergeFrom((com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover other) {
        if (other == com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover.getDefaultInstance()) return this;
        if (other.hasState()) {
          setState(other.getState());
        }
        if (other.hasPhoneNum()) {
          setPhoneNum(other.getPhoneNum());
        }
        if (other.hasName()) {
          setName(other.getName());
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
              setState(input.readInt32());
              break;
            }
            case 18: {
              setPhoneNum(input.readString());
              break;
            }
            case 26: {
              setName(input.readString());
              break;
            }
          }
        }
      }
      
      
      // required int32 state = 1;
      public boolean hasState() {
        return result.hasState();
      }
      public int getState() {
        return result.getState();
      }
      public Builder setState(int value) {
        result.hasState = true;
        result.state_ = value;
        return this;
      }
      public Builder clearState() {
        result.hasState = false;
        result.state_ = 0;
        return this;
      }
      
      // required string phoneNum = 2;
      public boolean hasPhoneNum() {
        return result.hasPhoneNum();
      }
      public java.lang.String getPhoneNum() {
        return result.getPhoneNum();
      }
      public Builder setPhoneNum(java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  result.hasPhoneNum = true;
        result.phoneNum_ = value;
        return this;
      }
      public Builder clearPhoneNum() {
        result.hasPhoneNum = false;
        result.phoneNum_ = getDefaultInstance().getPhoneNum();
        return this;
      }
      
      // required string name = 3;
      public boolean hasName() {
        return result.hasName();
      }
      public java.lang.String getName() {
        return result.getName();
      }
      public Builder setName(java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  result.hasName = true;
        result.name_ = value;
        return this;
      }
      public Builder clearName() {
        result.hasName = false;
        result.name_ = getDefaultInstance().getName();
        return this;
      }
    }
    
    static {
      com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.getDescriptor();
    }
    
    static {
      com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.internalForceInit();
    }
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_com_baidu_carlife_protobuf_CarlifeBTHfpCallStatusCover_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_com_baidu_carlife_protobuf_CarlifeBTHfpCallStatusCover_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n&CarlifeBTHfpCallStatusCoverProto.proto" +
      "\022\032com.baidu.carlife.protobuf\"L\n\033CarlifeB" +
      "THfpCallStatusCover\022\r\n\005state\030\001 \002(\005\022\020\n\010ph" +
      "oneNum\030\002 \002(\t\022\014\n\004name\030\003 \002(\t"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_com_baidu_carlife_protobuf_CarlifeBTHfpCallStatusCover_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_com_baidu_carlife_protobuf_CarlifeBTHfpCallStatusCover_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_com_baidu_carlife_protobuf_CarlifeBTHfpCallStatusCover_descriptor,
              new java.lang.String[] { "State", "PhoneNum", "Name", },
              com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover.class,
              com.baidu.carlife.protobuf.CarlifeBTHfpCallStatusCoverProto.CarlifeBTHfpCallStatusCover.Builder.class);
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
