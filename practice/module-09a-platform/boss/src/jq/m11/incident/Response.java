package jq.m11.incident;

/** What the rider's phone gets back for one request. */
public record Response(int status, String body) {

    /** 200: the request did its work. */
    public static Response ok(String body) {
        return new Response(200, body);
    }

    /** 503: this server is shutting down and takes no new requests. The load balancer tries another pod. */
    public static Response shuttingDown() {
        return new Response(503, "shutting down");
    }

    /** 502: the server stopped while this request was still running, so the answer never arrived. */
    public static Response cutOff() {
        return new Response(502, "connection closed mid-request");
    }
}
