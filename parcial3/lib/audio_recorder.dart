import 'dart:io';
import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:record/record.dart';
import 'package:path_provider/path_provider.dart';
import 'package:audioplayers/audioplayers.dart';

class AudioRecorderPage extends StatefulWidget {
  @override
  _AudioRecorderPageState createState() => _AudioRecorderPageState();
}

class _AudioRecorderPageState extends State<AudioRecorderPage> {
  final _recorder = Record();
  String? _filePath;
  final _player = AudioPlayer();
  bool _isRecording = false;

  Future<void> _checkPermissions() async {
    if (await Permission.microphone.request().isGranted &&
        await Permission.storage.request().isGranted) {
      return;
    }
    throw 'Permisos denegados';
  }

  Future<void> _startRecording() async {
    await _checkPermissions();
    final dir = await getApplicationDocumentsDirectory();
    final path = '${dir.path}/${DateTime.now().millisecondsSinceEpoch}.m4a';
    setState(() { _filePath = path; });
    await _recorder.start(
      path: path, // ruta de salida
      encoder: AudioEncoder.aacLc,
      bitRate: 128000,
    );
    setState(() { _isRecording = true; });
  }

  Future<void> _stopRecording() async {
    await _recorder.stop();
    setState(() { _isRecording = false; });
  }

  Future<void> _playRecording() async {
    if (_filePath != null && File(_filePath!).existsSync()) {
      await _player.play(DeviceFileSource(_filePath!));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Grabadora de Voz')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: _isRecording ? _stopRecording : _startRecording,
              child: Text(_isRecording ? 'Detener' : 'Grabar'),
            ),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: _playRecording,
              child: const Text('Reproducir último'),
            ),
          ],
        ),
      ),
    );
  }
}