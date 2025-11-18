package SPOTIFY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * P10 -> Spotify Music Player: Consolidated Java Code
 * This file contains all classes and interfaces modeled in the UML diagram.
 */

// --- 1. Song and Playlist (Data Model) ---

class Song {
    String name;
    String artist;
    String path;

    public Song(String name, String artist, String path) {
        this.name = name;
        this.artist = artist;
        this.path = path;
    }

    @Override
    public String toString() {
        return name + " by " + artist;
    }

    public String getPath() {
        return path;
    }
}

class Playlist {
    String name;
    List<Song> songs = new ArrayList<>();

    public Playlist(String name) {
        this.name = name;
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void printSongs() {
        System.out.println("Playlist: " + name);
        for (int i = 0; i < songs.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + songs.get(i));
        }
    }
}

// --- 2. Strategy Pattern for Playback ---

enum StrategyType {
    SEQUENTIAL, RANDOM, CUSTOM
}

abstract class PlayStrategy {
    abstract public void setPlaylist(Playlist p);
    abstract public Song nextSong();
    abstract public Song previousSong();
    abstract public void rewind();
}

class SequentialPlayStrategy extends PlayStrategy {
    private Playlist playlist;
    private int currentSongIndex = -1;

    @Override
    public void setPlaylist(Playlist p) {
        this.playlist = p;
        this.currentSongIndex = -1;
    }

    @Override
    public Song nextSong() {
        List<Song> songs = playlist.getSongs();
        if (songs.isEmpty()) return null;
        currentSongIndex = (currentSongIndex + 1) % songs.size();
        return songs.get(currentSongIndex);
    }

    @Override
    public Song previousSong() {
        List<Song> songs = playlist.getSongs();
        if (songs.isEmpty()) return null;
        currentSongIndex = (currentSongIndex - 1 + songs.size()) % songs.size();
        return songs.get(currentSongIndex);
    }

    @Override
    public void rewind() {
        currentSongIndex = -1;
    }
}

class RandomPlayStrategy extends PlayStrategy {
    private Playlist playlist;
    private Random random = new Random();

    @Override
    public void setPlaylist(Playlist p) {
        this.playlist = p;
    }

    @Override
    public Song nextSong() {
        List<Song> songs = playlist.getSongs();
        if (songs.isEmpty()) return null;
        return songs.get(random.nextInt(songs.size()));
    }

    @Override
    public Song previousSong() {
        // Random mode typically doesn't have a defined "previous," 
        // so we can implement it as just another random song for simplicity.
        return nextSong();
    }

    @Override
    public void rewind() {
        // No meaningful rewind for purely random playback.
    }
}

class CustomPlayStrategy extends PlayStrategy {
    private Playlist playlist;
    private List<Song> customOrder;
    private int currentSongIndex = -1;

    @Override
    public void setPlaylist(Playlist p) {
        this.playlist = p;
        // Example: Reverse order custom strategy
        this.customOrder = new ArrayList<>(p.getSongs());
        Collections.reverse(this.customOrder);
        this.currentSongIndex = -1;
    }

    @Override
    public Song nextSong() {
        if (customOrder.isEmpty()) return null;
        currentSongIndex = (currentSongIndex + 1) % customOrder.size();
        return customOrder.get(currentSongIndex);
    }

    @Override
    public Song previousSong() {
        if (customOrder.isEmpty()) return null;
        currentSongIndex = (currentSongIndex - 1 + customOrder.size()) % customOrder.size();
        return customOrder.get(currentSongIndex);
    }

    @Override
    public void rewind() {
        currentSongIndex = -1;
    }
}

// --- 3. Singleton for Strategy Management ---

class StrategyManager {
    private static StrategyManager instance;

    private StrategyManager() {}

    public static StrategyManager getInstance() {
        if (instance == null) {
            instance = new StrategyManager();
        }
        return instance;
    }

    public PlayStrategy getStrategy(StrategyType type) {
        switch (type) {
            case SEQUENTIAL: return new SequentialPlayStrategy();
            case RANDOM: return new RandomPlayStrategy();
            case CUSTOM: return new CustomPlayStrategy();
            default: return new SequentialPlayStrategy();
        }
    }
}

// --- 4. Audio Engine (Context for Strategy) ---

class AudioEngine {
    private PlayStrategy playStrategy;
    private int volume = 50;

    public void setPlayStrategy(PlayStrategy playStrategy, Playlist playlist) {
        this.playStrategy = playStrategy;
        this.playStrategy.setPlaylist(playlist);
        System.out.println("\nAudio Engine set to use: " + playStrategy.getClass().getSimpleName());
    }

    public Song playNext() {
        if (playStrategy == null) return null;
        Song next = playStrategy.nextSong();
        System.out.println("Audio Engine: Playing next song: " + next);
        return next;
    }

    public Song playPrevious() {
        if (playStrategy == null) return null;
        Song previous = playStrategy.previousSong();
        System.out.println("Audio Engine: Playing previous song: " + previous);
        return previous;
    }

    public void setVolume(int vol) {
        this.volume = vol;
        System.out.println("Audio Engine: Volume set to " + volume);
    }
}

// --- 5. Bridge Pattern for Device Playback (Abstraction and Implementation) ---

// Implementor Interface
abstract class InoutSpotifyDevice {
    abstract public void playAudio(Song song);
}

// Concrete Implementors (Device APIs)
class BluetoothSpeakerApi extends InoutSpotifyDevice {
    @Override
    public void playAudio(Song song) {
        System.out.println("    [Bluetooth API] Sending " + song.getPath() + " via Bluetooth.");
    }
}

class WiredSpeakerApi extends InoutSpotifyDevice {
    @Override
    public void playAudio(Song song) {
        System.out.println("    [Wired API] Sending " + song.getPath() + " over wired connection.");
    }
}

class HeadphoneSpeakerApi extends InoutSpotifyDevice {
    @Override
    public void playAudio(Song song) {
        System.out.println("    [Headphone API] Playing " + song.getPath() + " privately.");
    }
}

// Abstraction (Device Classes - the Bridge)
abstract class SpotifyDevice {
    protected InoutSpotifyDevice implementor;

    protected SpotifyDevice(InoutSpotifyDevice impl) {
        this.implementor = impl;
    }

    abstract public void play(Song song);
}

// Concrete Abstractions
class BluetoothSpeaker extends SpotifyDevice {
    public BluetoothSpeaker(InoutSpotifyDevice impl) { super(impl); }
    @Override
    public void play(Song song) {
        System.out.println("Device: Bluetooth Speaker initiated playback.");
        implementor.playAudio(song);
    }
}

class WiredSpeaker extends SpotifyDevice {
    public WiredSpeaker(InoutSpotifyDevice impl) { super(impl); }
    @Override
    public void play(Song song) {
        System.out.println("Device: Wired Speaker initiated playback.");
        implementor.playAudio(song);
    }
}

class HeadphoneSpeaker extends SpotifyDevice {
    public HeadphoneSpeaker(InoutSpotifyDevice impl) { super(impl); }
    @Override
    public void play(Song song) {
        System.out.println("Device: Headphone Speaker initiated playback.");
        implementor.playAudio(song);
    }
}

// --- 6. Factory Pattern for Device Creation ---

enum DeviceType {
    BLUETOOTH, WIRED, HEADPHONE
}

class DeviceFactory {
    public SpotifyDevice createDevice(DeviceType type) {
        switch (type) {
            case BLUETOOTH: return new BluetoothSpeaker(new BluetoothSpeakerApi());
            case WIRED: return new WiredSpeaker(new WiredSpeakerApi());
            case HEADPHONE: return new HeadphoneSpeaker(new HeadphoneSpeakerApi());
            default: return null;
        }
    }
}

// --- 7. Singleton for Device Management ---

class DeviceManager {
    private static DeviceManager instance;
    private SpotifyDevice currentDevice;
    private DeviceFactory factory = new DeviceFactory();

    private DeviceManager() {}

    public static DeviceManager getInstance() {
        if (instance == null) {
            instance = new DeviceManager();
        }
        return instance;
    }

    public void connectDevice(DeviceType type) {
        currentDevice = factory.createDevice(type);
        System.out.println("\nDeviceManager: Connected to " + type + " device.");
    }

    public SpotifyDevice getDevice() {
        return currentDevice;
    }
}

// --- 8. Facade Pattern (MusicPlayerFacade) ---

class MusicPlayerFacade {
    private static MusicPlayerFacade instance;
    private AudioEngine audioEngine = new AudioEngine();
    private DeviceManager deviceManager = DeviceManager.getInstance();
    private PlaylistManager playlistManager; // Set via constructor/setter or fetched internally
    private StrategyManager strategyManager = StrategyManager.getInstance();

    private MusicPlayerFacade(PlaylistManager pm) {
        this.playlistManager = pm;
    }

    public static MusicPlayerFacade getInstance(PlaylistManager pm) {
        if (instance == null) {
            instance = new MusicPlayerFacade(pm);
        }
        return instance;
    }

    public void play(String playlistName, StrategyType type) {
        System.out.println("\n--- FACADE: START PLAYBACK ---");
        Playlist playlist = playlistManager.getPlaylist(playlistName);
        if (playlist == null || playlist.getSongs().isEmpty()) {
            System.out.println("Facade: Error - Playlist not found or is empty.");
            return;
        }

        // 1. Set Strategy
        PlayStrategy strategy = strategyManager.getStrategy(type);
        audioEngine.setPlayStrategy(strategy, playlist);

        // 2. Get Next Song
        Song songToPlay = audioEngine.playNext();

        // 3. Play on Current Device
        SpotifyDevice device = deviceManager.getDevice();
        if (device != null && songToPlay != null) {
            device.play(songToPlay);
        } else {
            System.out.println("Facade: Error - No device connected or song to play.");
        }
        System.out.println("-----------------------------");
    }

    public void playNext() {
        Song songToPlay = audioEngine.playNext();
        SpotifyDevice device = deviceManager.getDevice();
        if (device != null && songToPlay != null) {
            device.play(songToPlay);
        }
    }

    public void playPrevious() {
        Song songToPlay = audioEngine.playPrevious();
        SpotifyDevice device = deviceManager.getDevice();
        if (device != null && songToPlay != null) {
            device.play(songToPlay);
        }
    }

    public void setVolume(int vol) {
        audioEngine.setVolume(vol);
    }
}

// --- 9. Playlist Manager (Helper for Facade) ---

class PlaylistManager {
    List<Playlist> playlists = new ArrayList<>();

    public Playlist createPlaylist(String name, List<Song> initialSongs) {
        Playlist p = new Playlist(name);
        for (Song song : initialSongs) {
            p.addSong(song);
        }
        playlists.add(p);
        return p;
    }

    public Playlist getPlaylist(String name) {
        for (Playlist p : playlists) {
            if (p.name.equals(name)) {
                return p;
            }
        }
        return null;
    }
}

// --- 10. Main Application (Client) ---

class MusicPlayerApplication {
    public static void main(String[] args) {
        System.out.println("--- 🎶 Spotify Music Player Client App Start 🎶 ---");

        // 1. Create Songs (Vector<Song> songlist)
        Song song1 = new Song("Blinding Lights", "The Weeknd", "/music/blinding.mp3");
        Song song2 = new Song("Levitating", "Dua Lipa", "/music/levitating.mp3");
        Song song3 = new Song("Save Your Tears", "The Weeknd", "/music/saveyourtears.mp3");
        List<Song> initialSongs = List.of(song1, song2, song3);

        // 2. Playlist Manager (Vector<String> playlistName, createPlaylist(String, String[]))
        PlaylistManager pm = new PlaylistManager();
        Playlist popHits = pm.createPlaylist("PopHits", initialSongs);
        popHits.printSongs();

        // 3. Device Manager (Singleton) and Device Factory (connectDevice(DeviceType))
        DeviceManager dm = DeviceManager.getInstance();
        dm.connectDevice(DeviceType.BLUETOOTH);

        // 4. MusicPlayerFacade (Singleton) - The main entry point
        MusicPlayerFacade player = MusicPlayerFacade.getInstance(pm);

        // 5. Playback Demo
        
        // A. Sequential Playback
        player.play("PopHits", StrategyType.SEQUENTIAL);
        player.playNext();
        player.playNext(); // Wraps around
        
        // B. Change Volume
        player.setVolume(75);

        // C. Change Device
        dm.connectDevice(DeviceType.HEADPHONE);
        
        // D. Random Playback
        player.play("PopHits", StrategyType.RANDOM);
        player.playNext();
        player.playPrevious(); // In random, this is another random song

        // E. Custom Playback (e.g., Reverse Order)
        player.play("PopHits", StrategyType.CUSTOM); // Plays 'Save Your Tears' first
        player.playNext(); // Plays 'Levitating'
    }
}
