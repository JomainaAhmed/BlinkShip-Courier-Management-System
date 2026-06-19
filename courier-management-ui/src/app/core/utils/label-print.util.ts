import { Shipment } from '../../models/shipment.model';

export class LabelPrintUtil {
  static printLabel(shipment: Shipment) {
    const printWindow = window.open('', '_blank');
    if (!printWindow) return;

    const content = `
      <html>
        <head>
          <title>Shipping Label - #${shipment.id || shipment.trackingNumber}</title>
          <style>
            body { font-family: 'Inter', sans-serif; padding: 40px; color: #000; }
            .label-container { border: 2px solid #000; padding: 20px; max-width: 500px; margin: auto; }
            .header { border-bottom: 2px solid #000; padding-bottom: 10px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }
            .logo { font-weight: 900; font-size: 24px; text-transform: uppercase; }
            .tracking-id { font-size: 18px; font-weight: bold; }
            .section { margin-bottom: 20px; }
            .label { font-size: 10px; font-weight: bold; text-transform: uppercase; color: #666; margin-bottom: 5px; }
            .value { font-size: 14px; font-weight: bold; }
            .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
            .footer { border-top: 1px solid #eee; pt-10 mt-20 text-center font-bold text-[10px] uppercase tracking-widest; }
          </style>
        </head>
        <body>
          <div class="label-container">
            <div class="header">
              <div class="logo">BLINK<span style="color: #FF6B00">SHIP</span></div>
              <div class="tracking-id">#${shipment.trackingNumber || shipment.id}</div>
            </div>
            
            <div class="grid">
              <div class="section">
                <div class="label">From (Sender)</div>
                <div class="value">${shipment.sender.name}</div>
                <div class="value">${shipment.sender.city}, ${shipment.sender.state}</div>
                <div class="value">${shipment.sender.pincode}</div>
              </div>
              <div class="section">
                <div class="label">To (Receiver)</div>
                <div class="value">${shipment.receiver.name}</div>
                <div class="value">${shipment.receiver.city}, ${shipment.receiver.state}</div>
                <div class="value">${shipment.receiver.pincode}</div>
              </div>
            </div>

            <div class="section" style="border-top: 1px dashed #ccc; padding-top: 20px;">
              <div class="label">Package Details</div>
              <div class="value">${shipment.packageDetails.description}</div>
              <div class="value">Weight: ${shipment.packageDetails.weight} KG</div>
            </div>

            <div class="footer">
              Thank you for choosing BlinkShip Logistics
            </div>
          </div>
          <script>window.print();</script>
        </body>
      </html>
    `;

    printWindow.document.write(content);
    printWindow.document.close();
  }
}
