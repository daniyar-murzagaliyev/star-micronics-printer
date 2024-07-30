import { WebPlugin } from '@capacitor/core';

import type { StarMicronicsPrinterPlugin } from './definitions';

export class StarMicronicsPrinterWeb
  extends WebPlugin
  implements StarMicronicsPrinterPlugin
{
  async scanDevices(): Promise<any> {
    return;
  }

  async connectPrinter(): Promise<any> {
    return;
  }
}
