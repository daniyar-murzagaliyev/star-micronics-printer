import Foundation
import Capacitor
import StarIO

@objc(StarMicronicsPrinterPlugin)
public class StarMicronicsPrinterPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "StarMicronicsPrinterPlugin"
    public let jsName = "StarMicronicsPrinter"
    var port: SMPort?

    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "scanDevices", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "connectPrinter", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = StarMicronicsPrinter()

    @objc func scanDevices(_ call: CAPPluginCall) {
        DispatchQueue.global(qos: .background).async {
            do {
                let portNames = try SMPort.searchPrinter(target: "BLE:") as? [PortInfo]
                var devices: [[String: String]] = []

                if let portNames = portNames {
                    for portInfo in portNames {
                        var deviceInfo: [String: String] = [:]
                        deviceInfo["name"] = portInfo.portName
                        deviceInfo["macAddress"] = portInfo.macAddress
                        devices.append(deviceInfo)
                    }
                }

                call.resolve([
                    "devices": devices
                ])
            } catch {
                call.reject("Failed to search for printers", error.localizedDescription)
            }
        }
    }

    @objc func connectPrinter(_ call: CAPPluginCall) {
        guard let portName = call.getString("portName") else {
            call.reject("Port name is required")
            return
        }

        do {
            self.port = try SMPort.getPort(portName: portName, portSettings: "", ioTimeoutMillis: 10000)
            call.resolve()
        } catch let error {
            call.reject("Failed to connect to printer", error.localizedDescription)
        }
    }

}

