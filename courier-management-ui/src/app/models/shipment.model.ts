export interface Address {
  id?: number;
  name: string;
  phone: string;
  addressLine: string;
  city: string;
  state: string;
  pincode: string;
}

export interface PackageDetails {
  id?: number;
  description: string;
  weight: number;
  length: number;
  width: number;
  height: number;
  price: number;
}

export interface Shipment {
  id?: number;
  status?: string;
  price?: number;
  sender: Address;
  receiver: Address;
  packageDetails: PackageDetails;
  trackingNumber?: string;
  createdAt?: string;
  updatedAt?: string;
  packedAt?: string;
  dispatchedAt?: string;
  inTransitAt?: string;
  deliveredAt?: string;
  activities?: any[];
}


export interface TrackingStep {
  id?: number;
  status: string;
  location: string;
  timestamp: string;
}

export interface TrackingInfo {
  shipment: Shipment;
  steps: TrackingStep[];
}
