export interface StarMicronicsPrinterPlugin {
  scanDevices(): Promise<any>;
  connectPrinter(): Promise<any>;
}
