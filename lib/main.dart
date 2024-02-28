import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:device_info_plus/device_info_plus.dart';
import 'package:flutter_barcode_scanner/flutter_barcode_scanner.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  var channel = const MethodChannel("scanQR");
  var barcode = '';
  var device = "";

  Future<void> showQRData() async {
    DeviceInfoPlugin deviceInfo = DeviceInfoPlugin();
    AndroidDeviceInfo androidInfo = await deviceInfo.androidInfo;

    print('Running on ${androidInfo.model}');

    if (androidInfo.model == "NLS-MT90") {
      device = "Running on ${androidInfo.model}";
      channel.invokeMethod("showQRData");
    } else {
      setState(() {
        device = "Running on ${androidInfo.model}";
      });
      String barcodeScanRes;

      try {
        barcodeScanRes = await FlutterBarcodeScanner.scanBarcode(
            '#ff6666', 'Cancel', true, ScanMode.QR);
        print(barcodeScanRes);
      } on PlatformException {
        barcodeScanRes = 'Failed to get platform version.';
      }

      if (!mounted) return;

      setState(() {
        barcode = barcodeScanRes;
      });
    }
  }

  @override
  void initState() {
    super.initState();
    channel.setMethodCallHandler((call) async {
      if (call.method == "onBarcodeScanned") {
        final barcode1 = call.arguments["barcode1"];
        final barcodeType = call.arguments["barcodeType"];

        print("Barcode 1: $barcode1");
        print("Barcode Type: $barcodeType");

        // Update the UI with the scanned barcode
        setState(() {
          barcode = "Scanned Barcode: $barcode1";
        });
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.secondary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: () {
                showQRData();
              },
              child: const Text('Simulate Scan'),
            ),
            const SizedBox(height: 20),
            Text(
              barcode,
              style: const TextStyle(fontSize: 18),
            ),
            Text(
              device,
              style: const TextStyle(fontSize: 18),
            ),
          ],
        ),
      ),
    );
  }
}
