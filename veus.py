#!/usr/bin/python3
# -*- coding: UTF8 -*-
"""
@created: 17-10-2025
@author: rafael
@description: Mostra de les veus del model Coqui tts

Instalació prèvia:
sudo apt-get install python-tk
sudo apt-get install python3-pil python3-pil.imagetk
pip3 install --user pydub speechrecognition
"""

import warnings
warnings.filterwarnings("ignore", message="pkg_resources is deprecated")

import threading
import queue
import tkinter as tk
from tkinter import ttk, filedialog
import speech_recognition as sr

import sys, os, re, glob, time, shutil
import torch
from TTS.api import TTS
import wave


class MostraDeVeus:
   def __init__(self, root):
      self.root = root
      self.root.title("Veus")
      self.root.minsize(800, 600)

      # Variables
      self.arxiu_wav = "tmp/tmp.wav"
      self.selected_voice = tk.StringVar(value="")
      self.dir_images = "static/img"
      self.images = {}
      self.default_state = "Fes clic a inici"
      self.status_text = tk.StringVar(value=self.default_state)
      self.tts = None
      self.voices = {}

      self.carrega_imatges()
      self.carrega_veus()
      self.create_widgets()

   def carrega_imatges(self):
      self.images['anterior'] = tk.PhotoImage(file=f"{self.dir_images}/anterior.png")
      self.images['inici'] = tk.PhotoImage(file=f"{self.dir_images}/inici.png")
      self.images['seguent'] = tk.PhotoImage(file=f"{self.dir_images}/seguent.png")
      self.images['sortir'] = tk.PhotoImage(file=f"{self.dir_images}/sortir.png")

   def carrega_veus(self):
      device = "cuda" if torch.cuda.is_available() else "cpu"
      self.tts = TTS("tts_models/ca/custom/vits", progress_bar=False).to(device)
      self.voices = self.tts.speakers

   def create_widgets(self):
      # Frame principal
      main_frame = ttk.Frame(self.root, padding="10")
      main_frame.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))

      # Configurar grid weights
      self.root.columnconfigure(0, weight=1)
      self.root.rowconfigure(0, weight=1)
      main_frame.columnconfigure(1, weight=1)
      main_frame.rowconfigure(4, weight=1)

      # Títol
      ttk.Label(main_frame, text="Mostra de les veus del model Coqui tts", font=("Arial", 16, "bold")).grid(row=0, column=0, columnspan=3, pady=(0, 10))

      # Selector de veus
      ttk.Label(main_frame, text="veu:", font=("Arial",9,"bold")).grid(row=2, column=0, sticky=(tk.N,tk.W), pady=(5,10))
      voice_frame = ttk.Frame(main_frame)
      voice_frame.grid(row=2, column=1, columnspan=2, sticky=(tk.N, tk.W, tk.W), pady=(5,10))
      voice_frame.columnconfigure(0, weight=1)

      # Combobox per seleccionar veu
      self.voice_combo = ttk.Combobox(
         voice_frame,
         values=list(self.voices.keys()),
         state="readonly",
         font=("Arial",9),
         width=16
      )
      self.voice_combo.grid(row=0, column=0, sticky=tk.W, padx=(0, 10))

      # Etiqueta que mostra el codi de la veu seleccionada
      self.idioma_actiu = ttk.Label(
         voice_frame,
         text=f"veu actual: {self.selected_voice.get()}",
         font=("Arial", 9),
         foreground="#0000a0"
      )
      self.idioma_actiu.grid(row=0, column=1, sticky=tk.W)

      # Vincular l'event de canvi de selecció
      self.voice_combo.bind('<<ComboboxSelected>>', self.on_voice_change)

      # Estat
      ttk.Label(main_frame, textvariable=self.status_text, font=("Arial",9,"italic")).grid(row=3, column=0, columnspan=3, sticky=(tk.N,tk.W))

      # Botons de control
      button_frame = ttk.Frame(main_frame)
      button_frame.grid(row=4, column=0, columnspan=3, sticky=tk.N, pady=(15,0))

      ttk.Button(button_frame, image=self.images['anterior'], command=self.anterior).pack(side=tk.LEFT, padx=5)
      ttk.Button(button_frame, image=self.images['inici'], command=self.text_to_audio).pack(side=tk.LEFT, padx=5)
      ttk.Button(button_frame, image=self.images['seguent'], command=self.seguent).pack(side=tk.LEFT, padx=5)
      ttk.Button(button_frame, image=self.images['sortir'], command=self.root.destroy).pack(side=tk.LEFT, padx=(10,0))


   def on_voice_change(self, event):
      '''Actualitza l'etiqueta del codi de veu quan canvia la selecció'''
      selected_voice_name = self.voice_combo.get()
      self.selected_voice.set(selected_voice_name)
      self.idioma_actiu.config(text=f"veu actual: {selected_voice_name}")
      self.status_text.set(f"Veu cambiada a: {selected_voice_name}")

   def text_to_audio(self, text, id_veu):
      #print("tts: ", self.tts)
      #print("tts.speakers: ", self.tts.speakers)

      # Text to speech list of amplitude values as output
      wav = self.tts.tts(text, speaker=id_veu)
      play(wav)

      # Text to speech to a file
      self.tts.tts_to_file(text, speaker=id_veu, file_path=self.arxiu_wav, verbose=False)
      #audio_pendent = AudioSegment.from_wav(self.arxiu_wav)
      #play(audio_pendent)

   def anterior(self):
      self.status_text.set(f"Escoltant [{self.selected_voice.get()}]")

   def seguent(self):
      self.status_text.set(f"Escoltant [{self.selected_voice.get()}]")

   def actualitza_estat(self, status):
      """Actualitza l'interfase amb el resultat del reconeixement de veu"""
      self.status_text.set(status)


if __name__ == "__main__":
   root = tk.Tk()
   MostraDeVeus(root)
   root.mainloop()
