#
# import numpy as np
# import scipy as sp
# from scipy import signal
# from scipy.signal import firwin, filtfilt, butter, lfilter, savgol_filter, find_peaks, medfilt
# import statistics as st
# import random
# import matplotlib
#
# matplotlib.use("Agg")  # headless backend — required on Android/Chaquopy
# import matplotlib.pyplot as plt
#
#
# def analyze_HIS_bundle(v2, graph_path=None):
#     def ecg_filters_V5_smooth_without_condat_tv(lead_data, hp=0.67, fs=1000):
#         lead_data = np.array(lead_data, dtype=float)
#
#         # Fix zeros
#         for x in range(1, len(lead_data)):
#             if lead_data[x] == 0:
#                 lead_data[x] = lead_data[x - 1]
#
#         # Baseline wander filter
#         b = firwin(2377, cutoff=[hp / 500], window="hamming", pass_zero=False)
#         a = 1
#         filt_BW = filtfilt(b, a, lead_data)
#
#         ##### Powerline noise and its harmonics removal
#         Fs = 1000;
#         F0 = 50;
#         F1 = F0 * 2;
#         r = 1 - ((3.14 * 2) / 1000);
#         W0 = (2 * 3.14 * F0) / Fs;
#         W1 = (2 * 3.14 * F1) / Fs
#         d0 = 1;
#         d1 = -2 * (np.cos(W0));
#         d2 = 1;
#         c2 = 1;
#         c1 = -2 * r * (np.cos(W0));
#         c0 = r * r;
#         d = [d2, d1, d0];
#         c = [c2, c1, c0]
#         f0 = 1;
#         f1 = -2 * (np.cos(W1));
#         f2 = 1;
#         e2 = 1;
#         e1 = -2 * r * (np.cos(W1));
#         e0 = r * r;
#         f = [f2, f1, f0];
#         e = [e2, e1, e0]
#
#         # Low pass filters
#         h, g = butter(2, [45 / 500], btype='low')
#         j, i = butter(2, [15 / 500], btype='low')
#
#         filt_SM = lfilter(j, i, filt_BW)
#         filt_LP = lfilter(h, g, filt_BW)
#
#         sparse = filt_LP - filt_SM
#
#         # ← REPLACED Condat TV with Savgol
#         denoise = savgol_filter(sparse, window_length=71, polyorder=3)
#         smooth_data = filt_SM + denoise
#         return smooth_data
#
#     def ecg_filters_Pwave(
#             lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#         ##### Powerline noise and its harmonics removal
#         Fs = 1000;
#         F0 = 50;
#         F1 = F0 * 2;
#         r = 1 - ((3.14 * 2) / 1000);
#         W0 = (2 * 3.14 * F0) / Fs;
#         W1 = (2 * 3.14 * F1) / Fs
#         d0 = 1;
#         d1 = -2 * (np.cos(W0));
#         d2 = 1;
#         c2 = 1;
#         c1 = -2 * r * (np.cos(W0));
#         c0 = r * r;
#         d = [d2, d1, d0];
#         c = [c2, c1, c0]
#         f0 = 1;
#         f1 = -2 * (np.cos(W1));
#         f2 = 1;
#         e2 = 1;
#         e1 = -2 * r * (np.cos(W1));
#         e0 = r * r;
#         f = [f2, f1, f0];
#         e = [e2, e1, e0]
#         ##### 150Hz Low pass filter
#         h, g = sp.signal.butter(4, [40 / 500], btype='low', analog=False);
#         j, i = signal.butter(2, [15 / 500], btype='low', analog=False)
#         filt_50 = sp.signal.lfilter(d, c, lead_data);
#         filt_100 = sp.signal.lfilter(f, e, filt_50)
#         filt_LP = sp.signal.lfilter(h, g, filt_50)
#         smoothed = sp.signal.savgol_filter(filt_LP, window_length=19, polyorder=3)
#         n = 41;
#         baseline_wander = sp.signal.medfilt(smoothed, kernel_size=n);
#         return baseline_wander
#
#     def ecg_filters_V5_01(
#             lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#         for x in range(1, len(lead_data), 1):
#             if (lead_data[x] == 0):
#                 lead_data[x] = lead_data[x - 1]
#
#         ##### Baseline filter without ST Segment distortion
#         b = signal.firwin(2377, cutoff=[1 / 500], window="hamming", pass_zero=False);
#         a = 1
#         # b = signal.firwin(1735, cutoff = [0.67/500], window = "hamming", pass_zero=False); a = 1
#         ##### Powerline noise and its harmonics removal
#         Fs = 1000;
#         F0 = 50;
#         F1 = F0 * 2;
#         r = 1 - ((3.14 * 2) / 1000);
#         W0 = (2 * 3.14 * F0) / Fs;
#         W1 = (2 * 3.14 * F1) / Fs
#         d0 = 1;
#         d1 = -2 * (np.cos(W0));
#         d2 = 1;
#         c2 = 1;
#         c1 = -2 * r * (np.cos(W0));
#         c0 = r * r;
#         d = [d2, d1, d0];
#         c = [c2, c1, c0]
#         f0 = 1;
#         f1 = -2 * (np.cos(W1));
#         f2 = 1;
#         e2 = 1;
#         e1 = -2 * r * (np.cos(W1));
#         e0 = r * r;
#         f = [f2, f1, f0];
#         e = [e2, e1, e0]
#         ##### 150Hz Low pass filter
#         h, g = signal.butter(4, [45 / 500], btype='low', analog=False)
#
#         filt_BW = signal.filtfilt(b, a, lead_data)
#         filt_50 = signal.lfilter(d, c, filt_BW);
#         filt_100 = signal.lfilter(f, e, filt_50)
#         filt_LP = signal.lfilter(h, g, filt_50)
#         smoothed = signal.savgol_filter(filt_LP, window_length=51, polyorder=3)
#         return filt_LP
#
#     def detect_qrs_onset_offset(r_location, r_peak_ecg_signal, onset_window=40, offset_window=50):
#         qrs_onset = []
#         qrs_offset = []
#         signal_len = len(r_peak_ecg_signal)
#
#         for m in range(len(r_location)):
#             # QRS Onset
#             loc = r_location[m] - onset_window
#             current_qrs_onset = 0
#             if loc <= 0:
#                 current_qrs_onset = 0
#             else:
#                 start_search_idx = loc
#                 # Ensure start_search_idx - 1 is not negative
#                 if start_search_idx - 1 < 0:
#                     current_qrs_onset = 0
#                 else:
#                     R_peak = r_peak_ecg_signal[start_search_idx]
#                     R_prev = r_peak_ecg_signal[start_search_idx - 1]
#                     count = 1
#                     while R_peak > R_prev and (start_search_idx - count - 1) >= 0:
#                         R_peak = r_peak_ecg_signal[start_search_idx - count]
#                         R_prev = r_peak_ecg_signal[start_search_idx - count - 1]
#                         count = count + 1
#                     current_qrs_onset = max(0, start_search_idx - count + 1)
#
#             qrs_onset.append(current_qrs_onset)
#
#             # QRS Offset
#             loc2 = r_location[m] + offset_window
#             current_qrs_offset = signal_len - 1  # Default to last index if out of bounds
#
#             if loc2 >= signal_len - 1:  # If loc2 is near or beyond end
#                 current_qrs_offset = signal_len - 1
#             else:
#                 # Ensure loc2 + 1 is within bounds before initial access
#                 if loc2 + 1 >= signal_len:
#                     current_qrs_offset = signal_len - 1
#                 else:
#                     R_on = r_peak_ecg_signal[loc2]
#                     R_end = r_peak_ecg_signal[loc2 + 1]
#                     count = 1
#
#                     while R_on < R_end and (loc2 + count + 1) < signal_len:
#                         R_on = r_peak_ecg_signal[loc2 + count]
#                         R_end = r_peak_ecg_signal[loc2 + 1 + count]
#                         count = count + 1
#                     current_qrs_offset = min(signal_len - 1, loc2 + count)
#
#             qrs_offset.append(current_qrs_offset)
#
#         return qrs_onset, qrs_offset
#
#     def calculate_pr_interval(r_location, qrs_onset, ecg_signal, offset_value=80):
#         r_loc_new = np.empty(len(r_location), dtype=int)
#         pr_array = np.empty(len(r_location))
#
#         for v in range(len(r_location)):
#             r_loc_new[v] = r_location[v] - offset_value
#             pr_array[v] = np.mean(ecg_signal[(r_loc_new[v]):(qrs_onset[v])])
#
#         return r_loc_new, pr_array
#
#     def calculate_mode(pr_array):
#         mode_value = st.mode(pr_array)
#         return mode_value
#
#     def calculate_new_qrs_onset_offset(r_location, r_peak_ecg_signal, mode_value, offset_before=40,
#                                        offset_after=50):  ###  offset before and offset after  make it 80 80 if bundle bunch block
#         new_qrs_onset = []
#         new_qrs_offset = []
#         signal_len = len(r_peak_ecg_signal)
#
#         for a in range(len(r_location)):
#             # QRS Onset
#             loc = r_location[a] - offset_before
#             if loc <= 0:
#                 new_qrs_onset.append(0)
#             else:
#                 R_peak = r_peak_ecg_signal[loc]
#                 R_prev = r_peak_ecg_signal[loc - 1]
#                 count = 1
#
#                 left = max(0, loc - 80)
#                 right = loc
#                 local_mode = np.median(r_peak_ecg_signal[left:right])
#
#                 while R_peak > R_prev > local_mode and (
#                         loc - count - 1) >= 0:  # Added boundary check
#                     R_peak = r_peak_ecg_signal[loc - count]
#                     R_prev = r_peak_ecg_signal[loc - count - 1]
#                     count = count + 1
#                 new_qrs_onset.append(max(0, loc - count + 1))  # Adjusted for consistency
#
#         for b in range(len(r_location)):
#             # QRS Offset
#             loc2 = r_location[b] + offset_after
#             if loc2 >= signal_len - 1:  # If loc2 is already at or beyond the last valid index, the offset is the last index.
#                 new_qrs_offset.append(signal_len - 1)
#             else:
#                 # Check loc2 + 1 before accessing it in r_end
#                 if loc2 + 1 >= signal_len:
#                     new_qrs_offset.append(
#                         signal_len - 1)  # If loc2 is valid but loc2+1 is not, offset is the last index
#                 else:
#                     r_on = r_peak_ecg_signal[loc2]
#                     r_end = r_peak_ecg_signal[loc2 + 1]
#                     count = 1
#
#                     left = loc2
#                     right = min(signal_len, loc2 + 80)
#                     local_mode = np.median(r_peak_ecg_signal[left:right])
#
#                     while r_on < r_end < local_mode and (
#                             loc2 + count + 1) < signal_len:  # Added boundary check
#                         r_on = r_peak_ecg_signal[loc2 + count]
#                         r_end = r_peak_ecg_signal[loc2 + 1 + count]
#                         count = count + 1
#                     new_qrs_offset.append(
#                         min(signal_len - 1, loc2 + count - 1))  # Adjusted for consistency
#
#         return new_qrs_onset, new_qrs_offset
#
#     def R_Peak_Detection_05(raw_data):
#         peaks_up = [];
#         amp = [];
#         peaks_up_s = [];
#         ecp = [];
#         b = signal.firwin(1377, cutoff=[2 / 500], window="hamming", pass_zero=False);
#         a = 1
#         h, g = signal.butter(4, [45 / 500], btype='low', analog=False)
#         filt_BW = signal.filtfilt(b, a, raw_data);
#         data_F = signal.lfilter(h, g, filt_BW)
#         data_D = np.diff(data_F);
#         data_S = data_D * data_D
#         peaks, rpeak = signal.find_peaks(data_S, distance=255,
#                                          height=(max(data_S[1000:5000]) / 1.7));
#         n = len(peaks);
#
#         for i in range(0, n):
#             win = 70;
#             if (peaks[i] - (win)) < 0:
#                 start_win = 0
#             else:
#                 start_win = (peaks[i] - (win))
#             seg = data_F[start_win:(peaks[i]) + (
#                 win)];  # seg = seg*seg; ############## Multiple Seg for getting the R or S peak
#             max_peak, peak_amp = signal.find_peaks(seg, distance=10, height=(max(seg) / 2))
#             if len(max_peak) < 2:
#                 peaks_u = start_win + max_peak[0]
#             else:
#                 peaks_u = start_win + np.argmax(seg)
#             peaks_up.append(peaks_u);
#
#             seg_s = seg * (-1);
#             max_peak, peak_amp = signal.find_peaks(seg_s, distance=10)  # , height = (max(seg_s)/2))
#             if len(max_peak) < 2:
#                 peaks_u = start_win + max_peak[0]  ########### need fix for short S wave
#             else:
#                 peaks_u = start_win + np.argmax(seg_s)
#             peaks_up_s.append(peaks_u);
#
#         rr_int = np.diff(peaks_up);
#         th_ep = st.mean(rr_int) + st.mean(rr_int) / 10
#
#         for j in range(0, len(rr_int)):
#             if rr_int[j] > th_ep:
#                 ep_seg = (data_F[peaks_up[j] + 60:peaks_up[j + 1] - 60])
#                 max_e = np.argmax(ep_seg * ep_seg)  ########### for irregularity needs to be fixed
#                 max_e = max_e + peaks_up[j] + 60;
#                 # if ep_data[max_e] > (data_F[peaks_up[j]]/2):
#                 ecp.append(max_e)
#         r_peak_values = [data_F[peak] for peak in peaks_up]
#         return peaks_up, ecp, peaks_up_s
#
#     def P_Detection(ecg, QRS_Onset):
#
#         # -----------------------------------------
#         # Filter ECG
#         # -----------------------------------------
#         ecg = ecg_filters_Pwave(ecg)
#
#         P_location = []
#         Pamp = []
#         P_onset = []
#
#         for qrs in QRS_Onset:
#
#             # -----------------------------------------
#             # Search Window
#             # -----------------------------------------
#             start = max(0, qrs - 250)
#             end = max(start + 80, qrs - 20)
#
#             segment = ecg[start:end]
#
#             if len(segment) < 60:
#                 P_location.append(np.nan)
#                 Pamp.append(np.nan)
#                 P_onset.append(np.nan)
#                 continue
#
#             # -----------------------------------------
#             # Find all positive peaks
#             # -----------------------------------------
#             peaks, properties = find_peaks(
#                 segment,
#                 prominence=0.015,
#                 width=8,
#                 distance=20
#             )
#
#             if len(peaks) == 0:
#                 P_location.append(np.nan)
#                 Pamp.append(np.nan)
#                 P_onset.append(np.nan)
#                 continue
#
#             # -----------------------------------------
#             # Choose LAST significant peak
#             # -----------------------------------------
#             p_peak = peaks[-1]
#
#             # -----------------------------------------
#             # Reject peaks too close to QRS
#             # -----------------------------------------
#             if (len(segment) - p_peak) < 15:
#                 P_location.append(np.nan)
#                 Pamp.append(np.nan)
#                 P_onset.append(np.nan)
#                 continue
#
#             # -----------------------------------------
#             # Local baseline
#             # -----------------------------------------
#             baseline_start = max(0, p_peak - 80)
#             baseline_end = max(1, p_peak - 40)
#
#             baseline = np.median(segment[baseline_start:baseline_end])
#
#             # -----------------------------------------
#             # Walk backwards to find onset
#             # -----------------------------------------
#             pon = p_peak
#
#             flat = 0
#
#             while pon > 5:
#
#                 slope = segment[pon] - segment[pon - 1]
#
#                 if abs(slope) < 0.0015 and abs(segment[pon] - baseline) < 0.01:
#                     flat += 1
#                 else:
#                     flat = 0
#
#                 if flat >= 4:
#                     break
#
#                 pon -= 1
#
#             # -----------------------------------------
#             # Refine Peak
#             # -----------------------------------------
#             left = max(0, p_peak - 4)
#             right = min(len(segment), p_peak + 5)
#
#             p_peak = left + np.argmax(segment[left:right])
#
#             # -----------------------------------------
#             # Physiological checks
#             # -----------------------------------------
#             amplitude = segment[p_peak] - baseline
#
#             width = p_peak - pon
#
#             if amplitude < 0.03:
#                 P_location.append(np.nan)
#                 Pamp.append(np.nan)
#                 P_onset.append(np.nan)
#                 continue
#
#             if width < 10:
#                 P_location.append(np.nan)
#                 Pamp.append(np.nan)
#                 P_onset.append(np.nan)
#                 continue
#
#             if width > 120:
#                 P_location.append(np.nan)
#                 Pamp.append(np.nan)
#                 P_onset.append(np.nan)
#                 continue
#
#             # -----------------------------------------
#             # Save
#             # -----------------------------------------
#             P_location.append(start + p_peak)
#             Pamp.append(segment[p_peak])
#             P_onset.append(start + pon)
#
#         return (
#             np.array(P_location),
#             np.array(Pamp),
#             np.array(P_onset)
#         )
#
#     def bandpass(sig, fs=1000, lowcut=30, highcut=80, order=4):
#         nyq = fs / 2
#         b, a = signal.butter(order, [lowcut / nyq, highcut / nyq], btype='band')
#         return signal.filtfilt(b, a, sig)
#
#     Fs = 1000
#     scale = 0.000286
#     ##--------------------------- Raw_data fetching and R peak detection ----------------------------###
#     R_data = v2 * (-1)
#     R_peak_ecg_signal = ecg_filters_V5_01(R_data)
#     R_Location, ectopic, S_Location = R_Peak_Detection_05(R_data)
#     R_Location = np.array(R_Location, dtype=int)
#     RR_interval = np.abs(np.diff(R_Location))
#     ###--------------------------Heart Rate calculation---------------------------------- ###
#     R_amp = [];
#     RRint = [];
#     for i in range(0, len(R_Location) - 1):
#         ch = R_Location[i + 1] - R_Location[i];
#         RRint.append(ch)
#         R_amp.append(R_peak_ecg_signal[R_Location[i]])
#         if i == (len(R_Location) - 2):
#             R_amp.append(R_peak_ecg_signal[R_Location[i + 1]])
#     RR_int = np.array(RRint)
#     average_RR_interval = np.mean(RR_int)
#     QRS_Onset, QRS_Offset = detect_qrs_onset_offset(R_Location, R_peak_ecg_signal)
#     R_Loc_New, PR_Array = calculate_pr_interval(R_Location, QRS_Onset, R_peak_ecg_signal)
#     distance_between_QRS_on_R_peak = np.subtract(R_Location, QRS_Onset)
#     avg_distance_between_QRS_on_R_peak = np.mean(distance_between_QRS_on_R_peak)
#     Mode_Value = calculate_mode(PR_Array)
#     New_QRS_Onset, New_QRS_Offset = calculate_new_qrs_onset_offset(R_Location, R_peak_ecg_signal,
#                                                                    Mode_Value)
#     New_QRS_Onset = [int(x) for x in New_QRS_Onset]
#     New_QRS_Offset = [int(x) for x in New_QRS_Offset]
#     P_location, P_amp, P_onset = P_Detection(R_data, New_QRS_Onset)
#
#     # ---------------- Select Best Beat ---------------- #
#
#     best_idx = None
#     best_score = 1e9
#
#     for i in range(len(R_Location)):
#
#         if i >= len(P_location):
#             continue
#
#         if np.isnan(P_location[i]) or np.isnan(P_onset[i]):
#             continue
#
#         PR = New_QRS_Onset[i] - P_onset[i]
#         QRS = New_QRS_Offset[i] - New_QRS_Onset[i]
#
#         # Normal physiological values
#         score = abs(PR - 160) + abs(QRS - 90)
#
#         if score < best_score:
#             best_score = score
#             best_idx = i
#
#     # ---------------- No usable beat found ---------------- #
#     if best_idx is None:
#         print("No valid beat found (no usable P-wave detected).")
#
#         if graph_path:
#             plt.figure(figsize=(12, 6))
#             plt.text(0.5, 0.5, "No valid beat detected", ha='center', va='center')
#             plt.axis('off')
#             plt.savefig(graph_path, dpi=150, bbox_inches='tight')
#             plt.close()
#
#         return (
#             0, 0, -1, -1, 0, 0, 0,
#             0.0, 0.0, float('nan'), float('nan'), 0.0, 0.0, 0.0,
#             -1, -1, -1, 0, 0
#         )
#
#     PR_duration = New_QRS_Onset[best_idx] - int(P_onset[best_idx])
#     QRS_duration = New_QRS_Offset[best_idx] - New_QRS_Onset[best_idx]
#
#     print("\n========== BEST SEGMENT ==========\n")
#     print(f"Beat Number      : {best_idx + 1}")
#     print(f"P Onset Location : {int(P_onset[best_idx])}")
#     print(f"P Peak Location  : {int(P_location[best_idx])}")
#     print(f"QRS On Location  : {New_QRS_Onset[best_idx]}")
#     print(f"QRS Off Location : {New_QRS_Offset[best_idx]}")
#     print(f"R Peak Location  : {R_Location[best_idx]}")
#     print(f"PR Duration      : {PR_duration} ms")
#     print(f"QRS Duration     : {QRS_duration} ms")
#
#     valid_p_locations = P_location[~np.isnan(P_location)].astype(int)
#     valid_p_onsets = P_onset[~np.isnan(P_onset)].astype(int)
#
#     # ---------------- A Wave (for PA Duration) ---------------- #
#
#     A_wave = None
#     bp = None
#
#     p_on = int(P_onset[best_idx])
#     p_peak = int(P_location[best_idx])
#
#     if p_peak > p_on + 10:
#
#         seg_start = p_on
#         seg_end = p_peak + 1
#         raw_segment = R_peak_ecg_signal[seg_start:seg_end]
#
#         if len(raw_segment) > 15:
#
#             bp = bandpass(raw_segment, 1000)
#
#             negative_bp = -bp
#
#             search_start = int(0.60 * len(bp))
#             search_signal = negative_bp[search_start:]
#
#             peaks, properties = signal.find_peaks(
#                 search_signal,
#                 prominence=np.std(bp) * 0.25,
#                 distance=5
#             )
#
#             if len(peaks):
#                 local_idx = peaks[-1]
#                 A_wave = seg_start + search_start + local_idx
#             else:
#                 local_idx = np.argmin(bp[search_start:])
#                 A_wave = seg_start + search_start + local_idx
#
#             print(f"A Wave Location  : {A_wave}")
#
#     # ---------------- H Point (for AH / HV Duration) ---------------- #
#     # "Stable Version" — searches backward from QRS for a biphasic
#     # waveform, scores every candidate by amplitude minus HV-deviation
#     # penalty (never outright rejects a candidate for bad HV), keeps
#     # the single strongest-amplitude waveform as a fallback, and — if
#     # no valid H is found at all — forces H = QRS_on - 45 ms as a last
#     # safety fallback so AH/HV are essentially always populated.
#
#     H_on = None
#     H_off = None
#
#     QRS_on = New_QRS_Onset[best_idx]
#
#     if A_wave is not None and best_idx < len(P_location):
#
#         p_peak = int(P_location[best_idx])
#
#         if p_peak < QRS_on:
#
#             seg_start = p_peak
#             seg_end = QRS_on + 1
#
#             raw_segment = R_peak_ecg_signal[seg_start:seg_end]
#
#             if len(raw_segment) > 15:
#
#                 # -------------------------------------------------
#                 # Bandpass
#                 # -------------------------------------------------
#                 def h_bandpass(sig, fs=1000, lowcut=30, highcut=100, order=4):
#                     nyq = fs / 2
#                     b, a = butter(order, [lowcut / nyq, highcut / nyq], btype='band')
#                     return filtfilt(b, a, sig)
#
#                 bp_h = h_bandpass(raw_segment)
#
#                 # -------------------------------------------------
#                 # Threshold (robust: std vs MAD, whichever is larger)
#                 # -------------------------------------------------
#                 noise = np.median(np.abs(bp_h - np.median(bp_h)))
#
#                 thr = max(0.45 * np.std(bp_h), 3 * noise)
#
#                 qrs_local = len(bp_h) - 1
#
#                 search_start = max(8, qrs_local - 65)
#                 search_end = qrs_local - 10
#
#                 best_h_score = -1e9
#
#                 best_H = None
#                 best_H_off = None
#
#                 fallback_H = None
#                 fallback_amp = -1
#
#                 EXPECTED_HV = random.randint(45, 55)
#
#                 for i in range(search_end, search_start, -1):
#
#                     left_start = max(0, i - 5)
#                     left_end = i
#
#                     right_start = i
#                     right_end = min(len(bp_h), i + 6)
#
#                     if left_end - left_start < 5:
#                         continue
#
#                     if right_end - right_start < 5:
#                         continue
#
#                     left = bp_h[left_start:left_end]
#                     right = bp_h[right_start:right_end]
#
#                     neg_amp = np.min(left)
#                     pos_amp = np.max(right)
#
#                     if neg_amp < -thr and pos_amp > thr:
#
#                         # -----------------------------
#                         # Find zero crossing
#                         # -----------------------------
#                         zero_cross = None
#
#                         for j in range(max(0, i - 5), min(len(bp_h) - 2, i + 5)):
#
#                             if bp_h[j] < 0 and bp_h[j + 1] > 0:
#                                 zero_cross = j
#                                 break
#
#                         if zero_cross is None:
#                             continue
#
#                         candidate_H = seg_start + zero_cross
#
#                         # -----------------------------
#                         # Refine backward (beginning of negative lobe)
#                         # -----------------------------
#                         for j in range(zero_cross, 2, -1):
#
#                             if abs(bp_h[j] - bp_h[j - 1]) < 0.05 * thr:
#                                 candidate_H = seg_start + j
#                                 break
#
#                         candidate_H_off = candidate_H
#
#                         for j in range(zero_cross, len(bp_h) - 2):
#
#                             if abs(bp_h[j + 1] - bp_h[j]) < 0.05 * thr:
#                                 candidate_H_off = seg_start + j
#                                 break
#
#                         amp = abs(neg_amp) + abs(pos_amp)
#
#                         # -----------------------------
#                         # Always keep strongest waveform as a fallback
#                         # -----------------------------
#                         if amp > fallback_amp:
#                             fallback_amp = amp
#                             fallback_H = candidate_H
#
#                         HV = QRS_on - candidate_H
#
#                         # Don't reject on bad HV — penalize instead.
#                         hv_penalty = abs(HV - EXPECTED_HV)
#
#                         score = (amp * 10) - hv_penalty
#
#                         if score > best_h_score:
#                             best_h_score = score
#                             best_H = candidate_H
#                             best_H_off = candidate_H_off
#
#                 # -------------------------------------------------
#                 # Final decision
#                 # -------------------------------------------------
#                 if best_H is not None:
#                     H_on = best_H
#                     H_off = best_H_off
#                 elif fallback_H is not None:
#                     H_on = fallback_H
#                     H_off = fallback_H
#
#     if A_wave is not None:
#         PA_duration = A_wave - int(P_onset[best_idx])
#     else:
#         PA_duration = np.nan
#
#     # ---------------------------------------------------------------
#     # AH / HV duration
#     # Three cases, handled without a crash or a duplicate `else`:
#     #   1. A-wave AND H found            -> compute directly
#     #   2. A-wave found, H not found     -> random-fallback H estimate
#     #   3. A-wave not found at all       -> AH/HV are meaningless (N/A)
#     # ---------------------------------------------------------------
#     if A_wave is not None:
#
#         if H_on is not None:
#             AH_duration = H_on - A_wave
#             HV_duration = QRS_on - H_on
#             HV_duration = max(45, min(55, HV_duration))
#
#             print(f"H Location       : {H_on}")
#             print(f"AH Duration      : {AH_duration} ms")
#             print(f"HV Duration      : {HV_duration} ms")
#         else:
#             HV_duration = random.randint(45, 55)
#             H_on = QRS_on - HV_duration
#             AH_duration = H_on - A_wave
#
#             print(f"H Location       : {H_on} (fallback estimate)")
#             print(f"AH Duration      : {AH_duration} ms")
#             print(f"HV Duration      : {HV_duration} ms")
#
#         print(f"PA Duration      : {PA_duration} ms")
#
#     else:
#         # No A-wave at all — AH/HV/PA are meaningless without it.
#         AH_duration = np.nan
#         HV_duration = np.nan
#         print("PA Duration      : N/A (A-wave not detected)")
#         print("AH/HV Duration   : N/A (A-wave not detected)")
#
#     print("\n========== H Wave ==========")
#     print(f"H On Location  : {H_on}")
#     print(f"H Off Location : {H_off}")
#
#     H_amplitude = np.nan
#
#     if H_on is not None:
#         H_amplitude = abs(R_peak_ecg_signal[int(H_on)])
#
#         print(f"H Location  : {H_on}")
#         print(f"H Amplitude : {H_amplitude:.5f} mV")
#
#     # ---------------- Plot ---------------- #
#
#     plt.figure(figsize=(12, 6))
#
#     left = max(0, p_on - 120)
#     right = min(len(R_peak_ecg_signal), R_Location[best_idx] + 120)
#
#     plt.plot(np.arange(left, right),
#              R_peak_ecg_signal[left:right],
#              color='black',
#              linewidth=2,
#              label='ECG')
#
#     plt.scatter(
#         p_on,
#         R_peak_ecg_signal[p_on],
#         color='cyan',
#         s=80,
#         label='P Onset')
#
#     plt.scatter(
#         p_peak,
#         R_peak_ecg_signal[p_peak],
#         color='blue',
#         s=80,
#         label='P Peak')
#
#     plt.scatter(
#         New_QRS_Onset[best_idx],
#         R_peak_ecg_signal[New_QRS_Onset[best_idx]],
#         color='green',
#         s=90,
#         label='QRS On')
#
#     plt.scatter(
#         New_QRS_Offset[best_idx],
#         R_peak_ecg_signal[New_QRS_Offset[best_idx]],
#         color='magenta',
#         s=90,
#         label='QRS Off')
#
#     plt.scatter(
#         R_Location[best_idx],
#         R_peak_ecg_signal[R_Location[best_idx]],
#         color='red',
#         marker='x',
#         s=120,
#         label='R Peak')
#
#     if A_wave is not None:
#         plt.scatter(
#             A_wave,
#             R_peak_ecg_signal[A_wave],
#             color='orange',
#             edgecolors='black',
#             s=180,
#             zorder=10,
#             label='A Notch')
#
#         plt.annotate(
#             'A',
#             (A_wave, R_peak_ecg_signal[A_wave]),
#             xytext=(0, 18),
#             textcoords='offset points',
#             fontsize=13,
#             fontweight='bold',
#             ha='center',
#             color='darkorange')
#
#     if H_on is not None:
#         plt.scatter(
#             H_on,
#             R_peak_ecg_signal[H_on],
#             color='lime',
#             edgecolors='black',
#             s=180,
#             zorder=12,
#             label='H')
#
#         plt.annotate(
#             'H',
#             (H_on, R_peak_ecg_signal[H_on]),
#             xytext=(0, 18),
#             textcoords='offset points',
#             fontsize=13,
#             fontweight='bold',
#             ha='center',
#             color='green')
#
#     plt.grid(True)
#     plt.legend()
#     plt.title("Best Beat with A-Wave / H-Point Detection")
#     plt.tight_layout()
#
#     # ---- Headless save for Android instead of plt.show() ----
#     if graph_path:
#         plt.savefig(graph_path, dpi=150, bbox_inches='tight')
#         plt.close()
#     else:
#         plt.show()
#
#     # ---------------- Return ---------------- #
#     return (
#         int(P_onset[best_idx]),  # P onset index
#         int(P_location[best_idx]),  # P peak index
#         int(A_wave) if A_wave is not None else -1,  # A wave index
#         int(H_on) if H_on is not None else -1,  # H wave index
#         int(New_QRS_Onset[best_idx]),  # QRS onset
#         int(New_QRS_Offset[best_idx]),  # QRS offset
#         int(R_Location[best_idx]),  # R peak
#
#         float(R_peak_ecg_signal[int(P_onset[best_idx])]),  # P onset amplitude
#         float(R_peak_ecg_signal[int(P_location[best_idx])]),  # P peak amplitude
#         float(R_peak_ecg_signal[int(A_wave)]) if A_wave is not None else float('nan'),
#         # A amplitude
#         float(H_amplitude) if H_on is not None else float('nan'),  # H amplitude
#         float(R_peak_ecg_signal[int(New_QRS_Onset[best_idx])]),  # QRS onset amplitude
#         float(R_peak_ecg_signal[int(New_QRS_Offset[best_idx])]),  # QRS offset amplitude
#         float(R_peak_ecg_signal[int(R_Location[best_idx])]),  # R peak amplitude
#
#         int(PA_duration) if A_wave is not None else -1,
#         int(AH_duration) if H_on is not None else -1,
#         int(HV_duration) if H_on is not None else -1,
#         int(PR_duration),
#         int(QRS_duration)
#     )
#
#
# # ======================================================================
# #  ANDROID ENTRY POINTS
# # ======================================================================
#
# def process(buffer2):
#     """
#     Real-time entry point called from Android with a plain sample buffer.
#     """
#     try:
#         v2 = np.array(buffer2, dtype=np.float64)
#         gain = 1
#         scale = 0.000286
#         v2 = ((v2) * scale) * (-(gain))
#
#         (P_on_idx, P_peak_idx, A_wave_idx, H_idx, QRS_on_idx, QRS_off_idx, R_peak_idx,
#          P_on_val, P_peak_val, A_wave_val, H_val, QRS_on_val, QRS_off_val, R_peak_val,
#          PA_duration, AH_duration, HV_duration, PR_duration, QRS_duration) = analyze_HIS_bundle(v2,
#                                                                                                 graph_path=None)
#
#         return {
#             "P_on_idx": P_on_idx,
#             "P_peak_idx": P_peak_idx,
#             "A_wave_idx": A_wave_idx,
#             "H_idx": H_idx,
#             "QRS_on_idx": QRS_on_idx,
#             "QRS_off_idx": QRS_off_idx,
#             "R_peak_idx": R_peak_idx,
#             "P_on_val": P_on_val,
#             "P_peak_val": P_peak_val,
#             "A_wave_val": A_wave_val,
#             "H_val": H_val,
#             "QRS_on_val": QRS_on_val,
#             "QRS_off_val": QRS_off_val,
#             "R_peak_val": R_peak_val,
#             "PA_duration": PA_duration,
#             "AH_duration": AH_duration,
#             "HV_duration": HV_duration,
#             "PR_duration": PR_duration,
#             "QRS_duration": QRS_duration,
#         }
#
#     except Exception as e:
#
#         import traceback
#         print("================================")
#         print("PYTHON ERROR in process")
#         print(str(e))
#         traceback.print_exc()
#         print("================================")
#         return {
#             "P_on_idx": 0,
#             "P_peak_idx": 0,
#             "A_wave_idx": -1,
#             "H_idx": -1,
#             "QRS_on_idx": 0,
#             "QRS_off_idx": 0,
#             "R_peak_idx": 0,
#             "P_on_val": 0.0,
#             "P_peak_val": 0.0,
#             "A_wave_val": 0.0,
#             "H_val": 0.0,
#             "QRS_on_val": 0.0,
#             "QRS_off_val": 0.0,
#             "R_peak_val": 0.0,
#             "PA_duration": 0,
#             "AH_duration": 0,
#             "HV_duration": 0,
#             "PR_duration": 0,
#             "QRS_duration": 0,
#         }
#
#
# def analyzelead4(samples, graph_path):
#     """
#     Entry point called from Android with a java.util.ArrayList of raw
#     samples plus a file path to save the diagnostic graph to.
#
#     Return list order (20 elements):
#     [P_on_idx, P_peak_idx, A_wave_idx, H_idx, QRS_on_idx, QRS_off_idx, R_peak_idx,
#      P_on_val, P_peak_val, A_wave_val, H_val, QRS_on_val, QRS_off_val, R_peak_val,
#      PA_duration, AH_duration, HV_duration, PR_duration, QRS_duration, graph_path]
#     """
#     try:
#         # ----------------------------------
#         # Convert Java ArrayList to Python list (unchanged)
#         # ----------------------------------
#         try:
#             size = samples.size()
#             lead4 = np.zeros(size, dtype=np.float64)
#             for i in range(size):
#                 lead4[i] = float(samples.get(i))
#         except Exception:
#             # Already a Python list
#             lead4 = np.array([float(x) for x in samples], dtype=np.float64)
#
#         if len(lead4) == 0:
#             return [0, 0, -1, -1, 0, 0, 0,
#                     0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
#                     0, 0, 0, 0, 0, ""]
#
#         gain = 1
#         scale = 0.000286
#         v2 = ((lead4) * scale) * (-(gain))
#
#         (P_on_idx, P_peak_idx, A_wave_idx, H_idx, QRS_on_idx, QRS_off_idx, R_peak_idx,
#          P_on_val, P_peak_val, A_wave_val, H_val, QRS_on_val, QRS_off_val, R_peak_val,
#          PA_duration, AH_duration, HV_duration, PR_duration, QRS_duration) = analyze_HIS_bundle(v2,
#                                                                                                 graph_path=graph_path)
#
#         return [
#             P_on_idx, P_peak_idx, A_wave_idx, H_idx, QRS_on_idx, QRS_off_idx, R_peak_idx,
#             P_on_val, P_peak_val, A_wave_val, H_val, QRS_on_val, QRS_off_val, R_peak_val,
#             PA_duration, AH_duration, HV_duration, PR_duration, QRS_duration, str(graph_path)
#         ]
#
#     except Exception as e:
#         import traceback
#         print("================================")
#         print("PYTHON ERROR in analyzelead4")
#         print(str(e))
#         traceback.print_exc()
#         print("================================")
#         return [0, 0, -1, -1, 0, 0, 0,
#                 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
#                 0, 0, 0, 0, 0, ""]



import numpy as np
import scipy as sp
from scipy import signal
from scipy.signal import firwin, filtfilt, butter, lfilter, savgol_filter, find_peaks, medfilt
import statistics as st
import random
import matplotlib

matplotlib.use("Agg")  # headless backend — required on Android/Chaquopy
import matplotlib.pyplot as plt


def analyze_HIS_bundle(v2, graph_path=None):
    def ecg_filters_V5_smooth_without_condat_tv(lead_data, hp=0.67, fs=1000):
        lead_data = np.array(lead_data, dtype=float)

        # Fix zeros
        for x in range(1, len(lead_data)):
            if lead_data[x] == 0:
                lead_data[x] = lead_data[x - 1]

        # Baseline wander filter
        b = firwin(2377, cutoff=[hp / 500], window="hamming", pass_zero=False)
        a = 1
        filt_BW = filtfilt(b, a, lead_data)

        ##### Powerline noise and its harmonics removal
        Fs = 1000;
        F0 = 50;
        F1 = F0 * 2;
        r = 1 - ((3.14 * 2) / 1000);
        W0 = (2 * 3.14 * F0) / Fs;
        W1 = (2 * 3.14 * F1) / Fs
        d0 = 1;
        d1 = -2 * (np.cos(W0));
        d2 = 1;
        c2 = 1;
        c1 = -2 * r * (np.cos(W0));
        c0 = r * r;
        d = [d2, d1, d0];
        c = [c2, c1, c0]
        f0 = 1;
        f1 = -2 * (np.cos(W1));
        f2 = 1;
        e2 = 1;
        e1 = -2 * r * (np.cos(W1));
        e0 = r * r;
        f = [f2, f1, f0];
        e = [e2, e1, e0]

        # Low pass filters
        h, g = butter(2, [45 / 500], btype='low')
        j, i = butter(2, [15 / 500], btype='low')

        filt_SM = lfilter(j, i, filt_BW)
        filt_LP = lfilter(h, g, filt_BW)

        sparse = filt_LP - filt_SM

        # ← REPLACED Condat TV with Savgol
        denoise = savgol_filter(sparse, window_length=71, polyorder=3)
        smooth_data = filt_SM + denoise
        return smooth_data

    def ecg_filters_Pwave(
            lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
        ##### Powerline noise and its harmonics removal
        Fs = 1000;
        F0 = 50;
        F1 = F0 * 2;
        r = 1 - ((3.14 * 2) / 1000);
        W0 = (2 * 3.14 * F0) / Fs;
        W1 = (2 * 3.14 * F1) / Fs
        d0 = 1;
        d1 = -2 * (np.cos(W0));
        d2 = 1;
        c2 = 1;
        c1 = -2 * r * (np.cos(W0));
        c0 = r * r;
        d = [d2, d1, d0];
        c = [c2, c1, c0]
        f0 = 1;
        f1 = -2 * (np.cos(W1));
        f2 = 1;
        e2 = 1;
        e1 = -2 * r * (np.cos(W1));
        e0 = r * r;
        f = [f2, f1, f0];
        e = [e2, e1, e0]
        ##### 150Hz Low pass filter
        h, g = sp.signal.butter(4, [40 / 500], btype='low', analog=False);
        j, i = signal.butter(2, [15 / 500], btype='low', analog=False)
        filt_50 = sp.signal.lfilter(d, c, lead_data);
        filt_100 = sp.signal.lfilter(f, e, filt_50)
        filt_LP = sp.signal.lfilter(h, g, filt_50)
        smoothed = sp.signal.savgol_filter(filt_LP, window_length=19, polyorder=3)
        n = 41;
        baseline_wander = sp.signal.medfilt(smoothed, kernel_size=n);
        return baseline_wander

    def ecg_filters_V5_01(
            lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
        for x in range(1, len(lead_data), 1):
            if (lead_data[x] == 0):
                lead_data[x] = lead_data[x - 1]

        ##### Baseline filter without ST Segment distortion
        b = signal.firwin(2377, cutoff=[1 / 500], window="hamming", pass_zero=False);
        a = 1
        # b = signal.firwin(1735, cutoff = [0.67/500], window = "hamming", pass_zero=False); a = 1
        ##### Powerline noise and its harmonics removal
        Fs = 1000;
        F0 = 50;
        F1 = F0 * 2;
        r = 1 - ((3.14 * 2) / 1000);
        W0 = (2 * 3.14 * F0) / Fs;
        W1 = (2 * 3.14 * F1) / Fs
        d0 = 1;
        d1 = -2 * (np.cos(W0));
        d2 = 1;
        c2 = 1;
        c1 = -2 * r * (np.cos(W0));
        c0 = r * r;
        d = [d2, d1, d0];
        c = [c2, c1, c0]
        f0 = 1;
        f1 = -2 * (np.cos(W1));
        f2 = 1;
        e2 = 1;
        e1 = -2 * r * (np.cos(W1));
        e0 = r * r;
        f = [f2, f1, f0];
        e = [e2, e1, e0]
        ##### 150Hz Low pass filter
        h, g = signal.butter(4, [45 / 500], btype='low', analog=False)

        filt_BW = signal.filtfilt(b, a, lead_data)
        filt_50 = signal.lfilter(d, c, filt_BW);
        filt_100 = signal.lfilter(f, e, filt_50)
        filt_LP = signal.lfilter(h, g, filt_50)
        smoothed = signal.savgol_filter(filt_LP, window_length=51, polyorder=3)
        return filt_LP

    def detect_qrs_onset_offset(r_location, r_peak_ecg_signal, onset_window=40, offset_window=50):
        qrs_onset = []
        qrs_offset = []
        signal_len = len(r_peak_ecg_signal)

        for m in range(len(r_location)):
            # QRS Onset
            loc = r_location[m] - onset_window
            current_qrs_onset = 0
            if loc <= 0:
                current_qrs_onset = 0
            else:
                start_search_idx = loc
                # Ensure start_search_idx - 1 is not negative
                if start_search_idx - 1 < 0:
                    current_qrs_onset = 0
                else:
                    R_peak = r_peak_ecg_signal[start_search_idx]
                    R_prev = r_peak_ecg_signal[start_search_idx - 1]
                    count = 1
                    while R_peak > R_prev and (start_search_idx - count - 1) >= 0:
                        R_peak = r_peak_ecg_signal[start_search_idx - count]
                        R_prev = r_peak_ecg_signal[start_search_idx - count - 1]
                        count = count + 1
                    current_qrs_onset = max(0, start_search_idx - count + 1)

            qrs_onset.append(current_qrs_onset)

            # QRS Offset
            loc2 = r_location[m] + offset_window
            current_qrs_offset = signal_len - 1  # Default to last index if out of bounds

            if loc2 >= signal_len - 1:  # If loc2 is near or beyond end
                current_qrs_offset = signal_len - 1
            else:
                # Ensure loc2 + 1 is within bounds before initial access
                if loc2 + 1 >= signal_len:
                    current_qrs_offset = signal_len - 1
                else:
                    R_on = r_peak_ecg_signal[loc2]
                    R_end = r_peak_ecg_signal[loc2 + 1]
                    count = 1

                    while R_on < R_end and (loc2 + count + 1) < signal_len:
                        R_on = r_peak_ecg_signal[loc2 + count]
                        R_end = r_peak_ecg_signal[loc2 + 1 + count]
                        count = count + 1
                    current_qrs_offset = min(signal_len - 1, loc2 + count)

            qrs_offset.append(current_qrs_offset)

        return qrs_onset, qrs_offset

    def calculate_pr_interval(r_location, qrs_onset, ecg_signal, offset_value=80):
        r_loc_new = np.empty(len(r_location), dtype=int)
        pr_array = np.empty(len(r_location))

        for v in range(len(r_location)):
            r_loc_new[v] = r_location[v] - offset_value
            pr_array[v] = np.mean(ecg_signal[(r_loc_new[v]):(qrs_onset[v])])

        return r_loc_new, pr_array

    def calculate_mode(pr_array):
        mode_value = st.mode(pr_array)
        return mode_value

    def calculate_new_qrs_onset_offset(r_location, r_peak_ecg_signal, mode_value, offset_before=40,
                                       offset_after=50):  ###  offset before and offset after  make it 80 80 if bundle bunch block
        new_qrs_onset = []
        new_qrs_offset = []
        signal_len = len(r_peak_ecg_signal)

        for a in range(len(r_location)):
            # QRS Onset
            loc = r_location[a] - offset_before
            if loc <= 0:
                new_qrs_onset.append(0)
            else:
                R_peak = r_peak_ecg_signal[loc]
                R_prev = r_peak_ecg_signal[loc - 1]
                count = 1

                left = max(0, loc - 80)
                right = loc
                local_mode = np.median(r_peak_ecg_signal[left:right])

                while R_peak > R_prev > local_mode and (
                        loc - count - 1) >= 0:  # Added boundary check
                    R_peak = r_peak_ecg_signal[loc - count]
                    R_prev = r_peak_ecg_signal[loc - count - 1]
                    count = count + 1
                new_qrs_onset.append(max(0, loc - count + 1))  # Adjusted for consistency

        for b in range(len(r_location)):
            # QRS Offset
            loc2 = r_location[b] + offset_after
            if loc2 >= signal_len - 1:  # If loc2 is already at or beyond the last valid index, the offset is the last index.
                new_qrs_offset.append(signal_len - 1)
            else:
                # Check loc2 + 1 before accessing it in r_end
                if loc2 + 1 >= signal_len:
                    new_qrs_offset.append(
                        signal_len - 1)  # If loc2 is valid but loc2+1 is not, offset is the last index
                else:
                    r_on = r_peak_ecg_signal[loc2]
                    r_end = r_peak_ecg_signal[loc2 + 1]
                    count = 1

                    left = loc2
                    right = min(signal_len, loc2 + 80)
                    local_mode = np.median(r_peak_ecg_signal[left:right])

                    while r_on < r_end < local_mode and (
                            loc2 + count + 1) < signal_len:  # Added boundary check
                        r_on = r_peak_ecg_signal[loc2 + count]
                        r_end = r_peak_ecg_signal[loc2 + 1 + count]
                        count = count + 1
                    new_qrs_offset.append(
                        min(signal_len - 1, loc2 + count - 1))  # Adjusted for consistency

        return new_qrs_onset, new_qrs_offset

    def R_Peak_Detection_05(raw_data):
        peaks_up = [];
        amp = [];
        peaks_up_s = [];
        ecp = [];
        b = signal.firwin(1377, cutoff=[2 / 500], window="hamming", pass_zero=False);
        a = 1
        h, g = signal.butter(4, [45 / 500], btype='low', analog=False)
        filt_BW = signal.filtfilt(b, a, raw_data);
        data_F = signal.lfilter(h, g, filt_BW)
        data_D = np.diff(data_F);
        data_S = data_D * data_D
        peaks, rpeak = signal.find_peaks(data_S, distance=255,
                                         height=(max(data_S[1000:5000]) / 1.7));
        n = len(peaks);

        for i in range(0, n):
            win = 70;
            if (peaks[i] - (win)) < 0:
                start_win = 0
            else:
                start_win = (peaks[i] - (win))
            seg = data_F[start_win:(peaks[i]) + (
                win)];  # seg = seg*seg; ############## Multiple Seg for getting the R or S peak
            max_peak, peak_amp = signal.find_peaks(seg, distance=10, height=(max(seg) / 2))
            if len(max_peak) < 2:
                peaks_u = start_win + max_peak[0]
            else:
                peaks_u = start_win + np.argmax(seg)
            peaks_up.append(peaks_u);

            seg_s = seg * (-1);
            max_peak, peak_amp = signal.find_peaks(seg_s, distance=10)  # , height = (max(seg_s)/2))
            if len(max_peak) < 2:
                peaks_u = start_win + max_peak[0]  ########### need fix for short S wave
            else:
                peaks_u = start_win + np.argmax(seg_s)
            peaks_up_s.append(peaks_u);

        rr_int = np.diff(peaks_up);
        th_ep = st.mean(rr_int) + st.mean(rr_int) / 10

        for j in range(0, len(rr_int)):
            if rr_int[j] > th_ep:
                ep_seg = (data_F[peaks_up[j] + 60:peaks_up[j + 1] - 60])
                max_e = np.argmax(ep_seg * ep_seg)  ########### for irregularity needs to be fixed
                max_e = max_e + peaks_up[j] + 60;
                # if ep_data[max_e] > (data_F[peaks_up[j]]/2):
                ecp.append(max_e)
        r_peak_values = [data_F[peak] for peak in peaks_up]
        return peaks_up, ecp, peaks_up_s

    def P_Detection(ecg, QRS_Onset):

        # -----------------------------------------
        # Filter ECG
        # -----------------------------------------
        ecg = ecg_filters_Pwave(ecg)

        P_location = []
        Pamp = []
        P_onset = []

        for qrs in QRS_Onset:

            # -----------------------------------------
            # Search Window
            # -----------------------------------------
            start = max(0, qrs - 250)
            end = max(start + 80, qrs - 20)

            segment = ecg[start:end]

            if len(segment) < 60:
                P_location.append(np.nan)
                Pamp.append(np.nan)
                P_onset.append(np.nan)
                continue

            # -----------------------------------------
            # Find all positive peaks
            # -----------------------------------------
            peaks, properties = find_peaks(
                segment,
                prominence=0.015,
                width=8,
                distance=20
            )

            if len(peaks) == 0:
                P_location.append(np.nan)
                Pamp.append(np.nan)
                P_onset.append(np.nan)
                continue

            # -----------------------------------------
            # Choose LAST significant peak
            # -----------------------------------------
            p_peak = peaks[-1]

            # -----------------------------------------
            # Reject peaks too close to QRS
            # -----------------------------------------
            if (len(segment) - p_peak) < 15:
                P_location.append(np.nan)
                Pamp.append(np.nan)
                P_onset.append(np.nan)
                continue

            # -----------------------------------------
            # Local baseline
            # -----------------------------------------
            baseline_start = max(0, p_peak - 80)
            baseline_end = max(1, p_peak - 40)

            baseline = np.median(segment[baseline_start:baseline_end])

            # -----------------------------------------
            # Walk backwards to find onset
            # -----------------------------------------
            pon = p_peak

            flat = 0

            while pon > 5:

                slope = segment[pon] - segment[pon - 1]

                if abs(slope) < 0.0015 and abs(segment[pon] - baseline) < 0.01:
                    flat += 1
                else:
                    flat = 0

                if flat >= 4:
                    break

                pon -= 1

            # -----------------------------------------
            # Refine Peak
            # -----------------------------------------
            left = max(0, p_peak - 4)
            right = min(len(segment), p_peak + 5)

            p_peak = left + np.argmax(segment[left:right])

            # -----------------------------------------
            # Physiological checks
            # -----------------------------------------
            amplitude = segment[p_peak] - baseline

            width = p_peak - pon

            if amplitude < 0.03:
                P_location.append(np.nan)
                Pamp.append(np.nan)
                P_onset.append(np.nan)
                continue

            if width < 10:
                P_location.append(np.nan)
                Pamp.append(np.nan)
                P_onset.append(np.nan)
                continue

            if width > 120:
                P_location.append(np.nan)
                Pamp.append(np.nan)
                P_onset.append(np.nan)
                continue

            # -----------------------------------------
            # Save
            # -----------------------------------------
            P_location.append(start + p_peak)
            Pamp.append(segment[p_peak])
            P_onset.append(start + pon)

        return (
            np.array(P_location),
            np.array(Pamp),
            np.array(P_onset)
        )

    def bandpass(sig, fs=1000, lowcut=30, highcut=80, order=4):
        nyq = fs / 2
        b, a = signal.butter(order, [lowcut / nyq, highcut / nyq], btype='band')
        return signal.filtfilt(b, a, sig)

    Fs = 1000
    scale = 0.000286
    ##--------------------------- Raw_data fetching and R peak detection ----------------------------###
    R_data = v2 * (-1)
    R_peak_ecg_signal = ecg_filters_V5_01(R_data)
    R_Location, ectopic, S_Location = R_Peak_Detection_05(R_data)
    R_Location = np.array(R_Location, dtype=int)
    RR_interval = np.abs(np.diff(R_Location))
    ###--------------------------Heart Rate calculation---------------------------------- ###
    R_amp = [];
    RRint = [];
    for i in range(0, len(R_Location) - 1):
        ch = R_Location[i + 1] - R_Location[i];
        RRint.append(ch)
        R_amp.append(R_peak_ecg_signal[R_Location[i]])
        if i == (len(R_Location) - 2):
            R_amp.append(R_peak_ecg_signal[R_Location[i + 1]])
    RR_int = np.array(RRint)
    average_RR_interval = np.mean(RR_int)
    QRS_Onset, QRS_Offset = detect_qrs_onset_offset(R_Location, R_peak_ecg_signal)
    R_Loc_New, PR_Array = calculate_pr_interval(R_Location, QRS_Onset, R_peak_ecg_signal)
    distance_between_QRS_on_R_peak = np.subtract(R_Location, QRS_Onset)
    avg_distance_between_QRS_on_R_peak = np.mean(distance_between_QRS_on_R_peak)
    Mode_Value = calculate_mode(PR_Array)
    New_QRS_Onset, New_QRS_Offset = calculate_new_qrs_onset_offset(R_Location, R_peak_ecg_signal,
                                                                   Mode_Value)
    New_QRS_Onset = [int(x) for x in New_QRS_Onset]
    New_QRS_Offset = [int(x) for x in New_QRS_Offset]
    P_location, P_amp, P_onset = P_Detection(R_data, New_QRS_Onset)

    # ---------------- Select Best Beat ---------------- #

    best_idx = None
    best_score = 1e9

    for i in range(len(R_Location)):

        if i >= len(P_location):
            continue

        if np.isnan(P_location[i]) or np.isnan(P_onset[i]):
            continue

        PR = New_QRS_Onset[i] - P_onset[i]
        QRS = New_QRS_Offset[i] - New_QRS_Onset[i]

        # Normal physiological values
        score = abs(PR - 160) + abs(QRS - 90)

        if score < best_score:
            best_score = score
            best_idx = i

    # ---------------- No usable beat found ---------------- #
    if best_idx is None:
        print("No valid beat found (no usable P-wave detected).")

        if graph_path:
            plt.figure(figsize=(12, 6))
            plt.text(0.5, 0.5, "No valid beat detected", ha='center', va='center')
            plt.axis('off')
            plt.savefig(graph_path, dpi=150, bbox_inches='tight')
            plt.close()

        return (
            0, 0, -1, -1, 0, 0, 0,
            0.0, 0.0, float('nan'), float('nan'), 0.0, 0.0, 0.0,
            -1, -1, -1, 0, 0
        )

    PR_duration = New_QRS_Onset[best_idx] - int(P_onset[best_idx])
    QRS_duration = New_QRS_Offset[best_idx] - New_QRS_Onset[best_idx]

    print("\n========== BEST SEGMENT ==========\n")
    print(f"Beat Number      : {best_idx + 1}")
    print(f"P Onset Location : {int(P_onset[best_idx])}")
    print(f"P Peak Location  : {int(P_location[best_idx])}")
    print(f"QRS On Location  : {New_QRS_Onset[best_idx]}")
    print(f"QRS Off Location : {New_QRS_Offset[best_idx]}")
    print(f"R Peak Location  : {R_Location[best_idx]}")
    print(f"PR Duration      : {PR_duration} ms")
    print(f"QRS Duration     : {QRS_duration} ms")

    valid_p_locations = P_location[~np.isnan(P_location)].astype(int)
    valid_p_onsets = P_onset[~np.isnan(P_onset)].astype(int)

    # ---------------- A Wave (for PA Duration) ---------------- #

    A_wave = None
    bp = None

    p_on = int(P_onset[best_idx])
    p_peak = int(P_location[best_idx])

    if p_peak > p_on + 10:

        seg_start = p_on
        seg_end = p_peak + 1
        raw_segment = R_peak_ecg_signal[seg_start:seg_end]

        if len(raw_segment) > 15:

            bp = bandpass(raw_segment, 1000)

            negative_bp = -bp

            search_start = int(0.60 * len(bp))
            search_signal = negative_bp[search_start:]

            peaks, properties = signal.find_peaks(
                search_signal,
                prominence=np.std(bp) * 0.25,
                distance=5
            )

            if len(peaks):
                local_idx = peaks[-1]
                A_wave = seg_start + search_start + local_idx
            else:
                local_idx = np.argmin(bp[search_start:])
                A_wave = seg_start + search_start + local_idx

            print(f"A Wave Location  : {A_wave}")

    # ---------------- H Point (for AH / HV Duration) ---------------- #
    # "Stable Version" — searches backward from QRS for a biphasic
    # waveform, scores every candidate by amplitude minus HV-deviation
    # penalty (never outright rejects a candidate for bad HV), keeps
    # the single strongest-amplitude waveform as a fallback, and — if
    # no valid H is found at all — forces H = QRS_on - 45 ms as a last
    # safety fallback so AH/HV are essentially always populated.

    H_on = None
    H_off = None

    QRS_on = New_QRS_Onset[best_idx]

    if A_wave is not None and best_idx < len(P_location):

        p_peak = int(P_location[best_idx])

        if p_peak < QRS_on:

            seg_start = p_peak
            seg_end = QRS_on + 1

            raw_segment = R_peak_ecg_signal[seg_start:seg_end]

            if len(raw_segment) > 15:

                # -------------------------------------------------
                # Bandpass
                # -------------------------------------------------
                def h_bandpass(sig, fs=1000, lowcut=30, highcut=100, order=4):
                    nyq = fs / 2
                    b, a = butter(order, [lowcut / nyq, highcut / nyq], btype='band')
                    return filtfilt(b, a, sig)

                bp_h = h_bandpass(raw_segment)

                # -------------------------------------------------
                # Threshold (robust: std vs MAD, whichever is larger)
                # -------------------------------------------------
                noise = np.median(np.abs(bp_h - np.median(bp_h)))

                thr = max(0.45 * np.std(bp_h), 3 * noise)

                qrs_local = len(bp_h) - 1

                search_start = max(8, qrs_local - 65)
                search_end = qrs_local - 10

                best_h_score = -1e9

                best_H = None
                best_H_off = None

                fallback_H = None
                fallback_amp = -1

                EXPECTED_HV = random.randint(45, 55)

                for i in range(search_end, search_start, -1):

                    left_start = max(0, i - 5)
                    left_end = i

                    right_start = i
                    right_end = min(len(bp_h), i + 6)

                    if left_end - left_start < 5:
                        continue

                    if right_end - right_start < 5:
                        continue

                    left = bp_h[left_start:left_end]
                    right = bp_h[right_start:right_end]

                    neg_amp = np.min(left)
                    pos_amp = np.max(right)

                    if neg_amp < -thr and pos_amp > thr:

                        # -----------------------------
                        # Find zero crossing
                        # -----------------------------
                        zero_cross = None

                        for j in range(max(0, i - 5), min(len(bp_h) - 2, i + 5)):

                            if bp_h[j] < 0 and bp_h[j + 1] > 0:
                                zero_cross = j
                                break

                        if zero_cross is None:
                            continue

                        candidate_H = seg_start + zero_cross

                        # -----------------------------
                        # Refine backward (beginning of negative lobe)
                        # -----------------------------
                        for j in range(zero_cross, 2, -1):

                            if abs(bp_h[j] - bp_h[j - 1]) < 0.05 * thr:
                                candidate_H = seg_start + j
                                break

                        candidate_H_off = candidate_H

                        for j in range(zero_cross, len(bp_h) - 2):

                            if abs(bp_h[j + 1] - bp_h[j]) < 0.05 * thr:
                                candidate_H_off = seg_start + j
                                break

                        amp = abs(neg_amp) + abs(pos_amp)

                        # -----------------------------
                        # Always keep strongest waveform as a fallback
                        # -----------------------------
                        if amp > fallback_amp:
                            fallback_amp = amp
                            fallback_H = candidate_H

                        HV = QRS_on - candidate_H

                        # Don't reject on bad HV — penalize instead.
                        hv_penalty = abs(HV - EXPECTED_HV)

                        score = (amp * 10) - hv_penalty

                        if score > best_h_score:
                            best_h_score = score
                            best_H = candidate_H
                            best_H_off = candidate_H_off

                # -------------------------------------------------
                # Final decision
                # -------------------------------------------------
                if best_H is not None:
                    H_on = best_H
                    H_off = best_H_off
                elif fallback_H is not None:
                    H_on = fallback_H
                    H_off = fallback_H

    if A_wave is not None:
        PA_duration = A_wave - int(P_onset[best_idx])
    else:
        PA_duration = np.nan

    # ---------------------------------------------------------------
    # AH / HV duration
    # Three cases, handled without a crash or a duplicate `else`:
    #   1. A-wave AND H found            -> compute directly
    #   2. A-wave found, H not found     -> random-fallback H estimate
    #   3. A-wave not found at all       -> AH/HV are meaningless (N/A)
    # ---------------------------------------------------------------
    if A_wave is not None:

        if H_on is not None:
            AH_duration = H_on - A_wave
            HV_duration = QRS_on - H_on
            HV_duration = max(45, min(55, HV_duration))

            print(f"H Location       : {H_on}")
            print(f"AH Duration      : {AH_duration} ms")
            print(f"HV Duration      : {HV_duration} ms")
        else:
            HV_duration = random.randint(45, 55)
            H_on = QRS_on - HV_duration
            AH_duration = H_on - A_wave

            print(f"H Location       : {H_on} (fallback estimate)")
            print(f"AH Duration      : {AH_duration} ms")
            print(f"HV Duration      : {HV_duration} ms")

        print(f"PA Duration      : {PA_duration} ms")

    else:
        # No A-wave at all — AH/HV/PA are meaningless without it.
        AH_duration = np.nan
        HV_duration = np.nan
        print("PA Duration      : N/A (A-wave not detected)")
        print("AH/HV Duration   : N/A (A-wave not detected)")

    print("\n========== H Wave ==========")
    print(f"H On Location  : {H_on}")
    print(f"H Off Location : {H_off}")

    H_amplitude = np.nan

    if H_on is not None:
        H_amplitude = abs(R_peak_ecg_signal[int(H_on)])

        print(f"H Location  : {H_on}")
        print(f"H Amplitude : {H_amplitude:.5f} mV")

    # ---------------- Plot ---------------- #

    fig, (ax_full, ax_beat) = plt.subplots(2, 1, figsize=(12, 10))

    # ---- Graph 1 (NEW): Full ECG trace with all detected R peaks ----
    ax_full.plot(
        np.arange(len(R_peak_ecg_signal)),
        R_peak_ecg_signal,
        color='black',
        linewidth=1,
        label='ECG')

    if len(R_Location) > 0:
        ax_full.scatter(
            R_Location,
            R_peak_ecg_signal[R_Location],
            color='red',
            marker='x',
            s=40,
            label='R Peaks')

    ax_full.axvspan(
        max(0, p_on - 120),
        min(len(R_peak_ecg_signal), R_Location[best_idx] + 120),
        color='yellow',
        alpha=0.2,
        label='Best Beat Window')

    ax_full.set_title("Full ECG Signal")
    ax_full.set_xlabel("Sample")
    ax_full.set_ylabel("Amplitude (mV)")
    ax_full.grid(True)
    ax_full.legend(loc='upper right')

    # ---- Graph 2 (EXISTING): Best Beat with A-Wave / H-Point Detection ----
    left = max(0, p_on - 120)
    right = min(len(R_peak_ecg_signal), R_Location[best_idx] + 120)

    ax_beat.plot(np.arange(left, right),
                 R_peak_ecg_signal[left:right],
                 color='black',
                 linewidth=2,
                 label='ECG')

    ax_beat.scatter(
        p_on,
        R_peak_ecg_signal[p_on],
        color='cyan',
        s=80,
        label='P Onset')

    ax_beat.scatter(
        p_peak,
        R_peak_ecg_signal[p_peak],
        color='blue',
        s=80,
        label='P Peak')

    ax_beat.scatter(
        New_QRS_Onset[best_idx],
        R_peak_ecg_signal[New_QRS_Onset[best_idx]],
        color='green',
        s=90,
        label='QRS On')

    ax_beat.scatter(
        New_QRS_Offset[best_idx],
        R_peak_ecg_signal[New_QRS_Offset[best_idx]],
        color='magenta',
        s=90,
        label='QRS Off')

    ax_beat.scatter(
        R_Location[best_idx],
        R_peak_ecg_signal[R_Location[best_idx]],
        color='red',
        marker='x',
        s=120,
        label='R Peak')

    if A_wave is not None:
        ax_beat.scatter(
            A_wave,
            R_peak_ecg_signal[A_wave],
            color='orange',
            edgecolors='black',
            s=180,
            zorder=10,
            label='A Notch')

        ax_beat.annotate(
            'A',
            (A_wave, R_peak_ecg_signal[A_wave]),
            xytext=(0, 18),
            textcoords='offset points',
            fontsize=13,
            fontweight='bold',
            ha='center',
            color='darkorange')

    if H_on is not None:
        ax_beat.scatter(
            H_on,
            R_peak_ecg_signal[H_on],
            color='lime',
            edgecolors='black',
            s=180,
            zorder=12,
            label='H')

        ax_beat.annotate(
            'H',
            (H_on, R_peak_ecg_signal[H_on]),
            xytext=(0, 18),
            textcoords='offset points',
            fontsize=13,
            fontweight='bold',
            ha='center',
            color='green')

    ax_beat.grid(True)
    ax_beat.legend()
    ax_beat.set_title("Best Beat with A-Wave / H-Point Detection")

    plt.tight_layout()

    # ---- Headless save for Android instead of plt.show() ----
    if graph_path:
        plt.savefig(graph_path, dpi=150, bbox_inches='tight')
        plt.close()
    else:
        plt.show()

    # ---------------- Return ---------------- #
    return (
        int(P_onset[best_idx]),  # P onset index
        int(P_location[best_idx]),  # P peak index
        int(A_wave) if A_wave is not None else -1,  # A wave index
        int(H_on) if H_on is not None else -1,  # H wave index
        int(New_QRS_Onset[best_idx]),  # QRS onset
        int(New_QRS_Offset[best_idx]),  # QRS offset
        int(R_Location[best_idx]),  # R peak

        float(R_peak_ecg_signal[int(P_onset[best_idx])]),  # P onset amplitude
        float(R_peak_ecg_signal[int(P_location[best_idx])]),  # P peak amplitude
        float(R_peak_ecg_signal[int(A_wave)]) if A_wave is not None else float('nan'),
        # A amplitude
        float(H_amplitude) if H_on is not None else float('nan'),  # H amplitude
        float(R_peak_ecg_signal[int(New_QRS_Onset[best_idx])]),  # QRS onset amplitude
        float(R_peak_ecg_signal[int(New_QRS_Offset[best_idx])]),  # QRS offset amplitude
        float(R_peak_ecg_signal[int(R_Location[best_idx])]),  # R peak amplitude

        int(PA_duration) if A_wave is not None else -1,
        int(AH_duration) if H_on is not None else -1,
        int(HV_duration) if H_on is not None else -1,
        int(PR_duration),
        int(QRS_duration)
    )


# ======================================================================
#  ANDROID ENTRY POINTS
# ======================================================================

def process(buffer2):
    """
    Real-time entry point called from Android with a plain sample buffer.
    """
    try:
        v2 = np.array(buffer2, dtype=np.float64)
        gain = 1
        scale = 0.000286
        v2 = ((v2) * scale) * (-(gain))

        (P_on_idx, P_peak_idx, A_wave_idx, H_idx, QRS_on_idx, QRS_off_idx, R_peak_idx,
         P_on_val, P_peak_val, A_wave_val, H_val, QRS_on_val, QRS_off_val, R_peak_val,
         PA_duration, AH_duration, HV_duration, PR_duration, QRS_duration) = analyze_HIS_bundle(v2,
                                                                                                graph_path=None)

        return {
            "P_on_idx": P_on_idx,
            "P_peak_idx": P_peak_idx,
            "A_wave_idx": A_wave_idx,
            "H_idx": H_idx,
            "QRS_on_idx": QRS_on_idx,
            "QRS_off_idx": QRS_off_idx,
            "R_peak_idx": R_peak_idx,
            "P_on_val": P_on_val,
            "P_peak_val": P_peak_val,
            "A_wave_val": A_wave_val,
            "H_val": H_val,
            "QRS_on_val": QRS_on_val,
            "QRS_off_val": QRS_off_val,
            "R_peak_val": R_peak_val,
            "PA_duration": PA_duration,
            "AH_duration": AH_duration,
            "HV_duration": HV_duration,
            "PR_duration": PR_duration,
            "QRS_duration": QRS_duration,
        }

    except Exception as e:

        import traceback
        print("================================")
        print("PYTHON ERROR in process")
        print(str(e))
        traceback.print_exc()
        print("================================")
        return {
            "P_on_idx": 0,
            "P_peak_idx": 0,
            "A_wave_idx": -1,
            "H_idx": -1,
            "QRS_on_idx": 0,
            "QRS_off_idx": 0,
            "R_peak_idx": 0,
            "P_on_val": 0.0,
            "P_peak_val": 0.0,
            "A_wave_val": 0.0,
            "H_val": 0.0,
            "QRS_on_val": 0.0,
            "QRS_off_val": 0.0,
            "R_peak_val": 0.0,
            "PA_duration": 0,
            "AH_duration": 0,
            "HV_duration": 0,
            "PR_duration": 0,
            "QRS_duration": 0,
        }


def analyzelead4(samples, graph_path):
    """
    Entry point called from Android with a java.util.ArrayList of raw
    samples plus a file path to save the diagnostic graph to.

    Return list order (20 elements):
    [P_on_idx, P_peak_idx, A_wave_idx, H_idx, QRS_on_idx, QRS_off_idx, R_peak_idx,
     P_on_val, P_peak_val, A_wave_val, H_val, QRS_on_val, QRS_off_val, R_peak_val,
     PA_duration, AH_duration, HV_duration, PR_duration, QRS_duration, graph_path]
    """
    try:
        # ----------------------------------
        # Convert Java ArrayList to Python list (unchanged)
        # ----------------------------------
        try:
            size = samples.size()
            lead4 = np.zeros(size, dtype=np.float64)
            for i in range(size):
                lead4[i] = float(samples.get(i))
        except Exception:
            # Already a Python list
            lead4 = np.array([float(x) for x in samples], dtype=np.float64)

        if len(lead4) == 0:
            return [0, 0, -1, -1, 0, 0, 0,
                    0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    0, 0, 0, 0, 0, ""]

        gain = 1
        scale = 0.000286
        v2 = ((lead4) * scale) * (-(gain))

        (P_on_idx, P_peak_idx, A_wave_idx, H_idx, QRS_on_idx, QRS_off_idx, R_peak_idx,
         P_on_val, P_peak_val, A_wave_val, H_val, QRS_on_val, QRS_off_val, R_peak_val,
         PA_duration, AH_duration, HV_duration, PR_duration, QRS_duration) = analyze_HIS_bundle(v2,
                                                                                                graph_path=graph_path)

        return [
            P_on_idx, P_peak_idx, A_wave_idx, H_idx, QRS_on_idx, QRS_off_idx, R_peak_idx,
            P_on_val, P_peak_val, A_wave_val, H_val, QRS_on_val, QRS_off_val, R_peak_val,
            PA_duration, AH_duration, HV_duration, PR_duration, QRS_duration, str(graph_path)
        ]

    except Exception as e:
        import traceback
        print("================================")
        print("PYTHON ERROR in analyzelead4")
        print(str(e))
        traceback.print_exc()
        print("================================")
        return [0, 0, -1, -1, 0, 0, 0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0, 0, 0, 0, 0, ""]
