/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.application.connector.action;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.xcontent.ToXContentObject;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xpack.application.connector.Connector;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

import static org.elasticsearch.action.ValidateActions.addValidationError;

public class UpdateConnectorLastSeenAction extends ActionType<UpdateConnectorLastSeenAction.Response> {

    public static final UpdateConnectorLastSeenAction INSTANCE = new UpdateConnectorLastSeenAction();
    public static final String NAME = "cluster:admin/xpack/connector/update_last_seen";

    public UpdateConnectorLastSeenAction() {
        super(NAME, UpdateConnectorLastSeenAction.Response::new);
    }

    public static class Request extends ActionRequest implements ToXContentObject {

        private final String connectorId;

        private final Instant lastSeen;

        public Request(String connectorId) {
            this.connectorId = connectorId;
            this.lastSeen = Instant.now();
        }

        public Request(StreamInput in) throws IOException {
            super(in);
            this.connectorId = in.readString();
            this.lastSeen = in.readInstant();
        }

        public String getConnectorId() {
            return connectorId;
        }

        @Override
        public ActionRequestValidationException validate() {
            ActionRequestValidationException validationException = null;

            if (Strings.isNullOrEmpty(connectorId)) {
                validationException = addValidationError("[connector_id] cannot be null or empty.", validationException);
            }

            return validationException;
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            {
                builder.field(Connector.LAST_SEEN_FIELD.getPreferredName(), lastSeen);
            }
            builder.endObject();
            return builder;
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeString(connectorId);
            out.writeInstant(lastSeen);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Request request = (Request) o;
            return Objects.equals(connectorId, request.connectorId) && Objects.equals(lastSeen, request.lastSeen);
        }

        @Override
        public int hashCode() {
            return Objects.hash(connectorId, lastSeen);
        }
    }

    public static class Response extends ActionResponse implements ToXContentObject {

        final DocWriteResponse.Result result;

        public Response(StreamInput in) throws IOException {
            super(in);
            result = DocWriteResponse.Result.readFrom(in);
        }

        public Response(DocWriteResponse.Result result) {
            this.result = result;
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            this.result.writeTo(out);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            builder.field("result", this.result.getLowercase());
            builder.endObject();
            return builder;
        }

        public RestStatus status() {
            return switch (result) {
                case NOT_FOUND -> RestStatus.NOT_FOUND;
                default -> RestStatus.OK;
            };
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Response that = (Response) o;
            return Objects.equals(result, that.result);
        }

        @Override
        public int hashCode() {
            return Objects.hash(result);
        }
    }
}
