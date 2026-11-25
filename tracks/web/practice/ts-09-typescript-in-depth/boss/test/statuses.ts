import type { OrderStatus } from "../src/orderStatus.ts";

export const placed: OrderStatus = { kind: "PLACED" };
export const packed: OrderStatus = { kind: "PACKED", packedBy: "Raju" };
export const shipped: OrderStatus = { kind: "SHIPPED", courier: "Swift Couriers" };
export const delivered: OrderStatus = { kind: "DELIVERED", deliveredOn: "14 Nov" };
export const cancelled: OrderStatus = { kind: "CANCELLED", reason: "changed my mind" };
export const returned: OrderStatus = { kind: "RETURNED", refundPaise: 45000 };

/** A status an old server still sends, which the tracker has never heard of. */
export const lost = { kind: "LOST" } as unknown as OrderStatus;
