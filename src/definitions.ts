export interface StarMicronicsPrinterPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
