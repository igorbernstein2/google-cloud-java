// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: google/cloud/kms/v1/service.proto

package com.google.cloud.kms.v1;

public interface AsymmetricDecryptResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:google.cloud.kms.v1.AsymmetricDecryptResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The decrypted data originally encrypted with the matching public key.
   * </pre>
   *
   * <code>bytes plaintext = 1;</code>
   */
  com.google.protobuf.ByteString getPlaintext();
}
