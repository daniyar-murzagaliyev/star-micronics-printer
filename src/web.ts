import { WebPlugin } from '@capacitor/core';

import type { StarMicronicsPrinterPlugin } from './definitions';

export class StarMicronicsPrinterWeb
  extends WebPlugin
  implements StarMicronicsPrinterPlugin
{
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
