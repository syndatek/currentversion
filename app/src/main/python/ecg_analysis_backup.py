


# working block this
#
# import math
# import collections
# import datetime
# import numpy as np
# import scipy as sp
# from scipy import signal
# from scipy.signal import butter, filtfilt
# import statistics as st
# import pandas as pd
# from pytz import timezone
#
# try:
#     import condat_tv
# except ImportError:
#     condat_tv = None
#     print("WARNING: condat_tv not available on this device — "
#           "sparse/TV denoise step will be skipped for this run.")
#
#
# ## data loss correction ##
#
# def data_loss(input_signal):
#     for x in range(1, len(input_signal) - 1, 1):  #### data loss
#         if (input_signal[
#             x] == 0):  ## checks zero values in input signal and replaces with the mean of adjacent non zero values
#             input_signal[x] = st.mean([input_signal[x - 1], input_signal[x + 1]])
#     return (input_signal)
#
#
# ## time stamp ##
# def get_time_from_timestamp(
#         timestamp):  ## converting unit timestamp to a readable date and time format
#     read_able = datetime.datetime.fromtimestamp(timestamp)
#     now_asia = str(read_able.astimezone(timezone('Asia/Kolkata')));  # print(now_asia)
#     year = now_asia[0:10]
#     year = year[8:10] + "-" + year[5:7] + "-" + year[0:4]
#     time = now_asia[11:19]
#     return (year, time)
#
#
# ## function extracts values from a dictionary given a specific index ##
# def Value_axis(data_dict, value):
#     data = list(data_dict.items())
#     an_array = np.array(data, dtype=object)
#     Values_1 = an_array[value]
#     Values_2 = np.asarray(Values_1[1])
#     return Values_2
#
#
# ## smoothening filter: Baseline filter+powerline noise+150 hz low pass filter ##
# def ecg_filters_V5_smooth(lead_data,
#                           hp=0.67):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     for x in range(1, len(lead_data), 1):
#         if (lead_data[x] == 0):
#             lead_data[x] = lead_data[x - 1]
#
#     ##### Baseline filter without ST Segment distortion
#     b = signal.firwin(2377, cutoff=[hp / 500], window="hamming", pass_zero=False);
#     a = 1
#     # b = signal.firwin(1735, cutoff = [0.67/500], window = "hamming", pass_zero=False); a = 1
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     # h, g = signal.butter(2, [45/500] , btype='low', analog=False)
#     h, g = signal.butter(2, [45 / 500], btype='low', analog=False)
#     j, i = signal.butter(2, [15 / 500], btype='low', analog=False)
#
#     filt_BW = signal.filtfilt(b, a, lead_data);
#     filt_SM = (signal.lfilter(j, i, filt_BW))
#     filt_LP = (signal.lfilter(h, g, filt_BW))
#
#     sparse = filt_LP - filt_SM;
#     if condat_tv is not None:
#         denoise = condat_tv.tv_denoise(sparse, 6.5)
#     else:
#         denoise = sparse  # fallback if the native lib isn't bundled on device
#     smooth_data = filt_SM + denoise
#
#     return smooth_data
#
#
# def baseline_filter(lead_data, hp=0.67):
#     ##### Baseline filter without ST Segment distortion
#     b = signal.firwin(2377, cutoff=[hp / 500], window="hamming", pass_zero=False);
#     a = 1
#     filt_BW = signal.filtfilt(b, a, lead_data);
#     return filt_BW
#
#
# def ecg_filters_V5_01(
#         lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     for x in range(1, len(lead_data), 1):
#         if (lead_data[x] == 0):
#             lead_data[x] = lead_data[x - 1]
#
#     ##### Baseline filter without ST Segment distortion
#     b = signal.firwin(2377, cutoff=[1 / 500], window="hamming", pass_zero=False);
#     a = 1
#     # b = signal.firwin(1735, cutoff = [0.67/500], window = "hamming", pass_zero=False); a = 1
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     h, g = signal.butter(4, [45 / 500], btype='low', analog=False)
#
#     filt_BW = signal.filtfilt(b, a, lead_data)
#     filt_50 = signal.lfilter(d, c, filt_BW);
#     filt_100 = signal.lfilter(f, e, filt_50)
#     filt_LP = signal.lfilter(h, g, filt_50)
#     smoothed = signal.savgol_filter(filt_LP, window_length=29, polyorder=3)
#     return filt_LP
#
#
# def phasor_transform(signal_in, Rv):
#     PT = np.empty_like(signal_in, dtype=float)
#     for i in range(len(signal_in)):
#         ch = signal_in[i] / Rv
#         PT[i] = math.degrees(math.atan(ch))
#         # PT[i] = Rv+ ()
#     return (PT)
#
#
# def find_PVC(signal_in, Rpeaks, Ramplitued, RRinterval):
#     ##   LIST OF AREA UNDER THE CURVE
#     AUC = collections.deque()
#     n_sig = len(signal_in)
#     for k in Rpeaks:
#         # Guard against negative slice start wrapping around to the END
#         # of the array (Python silently allows signal_in[-5:10], which
#         # silently corrupts the AUC/PVC score for early beats instead
#         # of raising an error).
#         lo = max(0, k - 80)
#         hi = min(n_sig, k + 80)
#         ch = abs(np.trapz(signal_in[lo:hi]));
#         AUC.append(ch)
#         # print("median : ",ch, 1.3*st.median(AUC))
#     AUC = np.array(AUC);  # print(len(AUC),AUC)
#     ##    ECTOPIC FUNCTION
#     PVC_list = np.empty_like(Rpeaks, dtype=float);
#     PVC_list[:] = np.nan
#     SVC_list = np.empty_like(Rpeaks, dtype=float);
#     SVC_list[:] = np.nan
#     for i in range(1, len(RRinterval)):
#         if RRinterval[i] >= (1.5 * RRinterval[i - 1]):
#             if Ramplitued[i] >= (Ramplitued[i - 1] + (0.2 * Ramplitued[i - 1])):  # PVC
#                 # Guard: at i == 1, AUC[0:i-1] == AUC[0:0] == empty,
#                 # and statistics.median() raises StatisticsError on an
#                 # empty sequence. There's no preceding-beat baseline to
#                 # compare against yet, so skip the PVC check for the
#                 # very first comparable beat instead of crashing the
#                 # whole buffer's analysis.
#                 if i > 1 and AUC[i] > (1.3 * st.median(AUC[0:i - 1])):
#                     PVC_list[i] = Rpeaks[i]
#             else:  # SVC or PVC
#                 if i > 1:
#                     if AUC[i] > (1.3 * st.median(AUC[0:i - 1])):
#                         PVC_list[i] = Rpeaks[i]
#     return np.array(PVC_list)
#
#
# def ecg_filters_Pwave(
#         lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     h, g = sp.signal.butter(4, [40 / 500], btype='low', analog=False);
#     j, i = signal.butter(2, [15 / 500], btype='low', analog=False)
#     filt_50 = sp.signal.lfilter(d, c, lead_data);
#     filt_100 = sp.signal.lfilter(f, e, filt_50)
#     filt_LP = sp.signal.lfilter(h, g, filt_50)
#     smoothed = sp.signal.savgol_filter(filt_LP, window_length=19, polyorder=3)
#     n = 41;
#     baseline_wander = sp.signal.medfilt(smoothed, kernel_size=n);
#     return baseline_wander
#
#
# def calculate_p_offset_p_duration_and_average_pr(p_loc, r_peak_ecg_signal, qrs_onset,
#                                                  window_size=60):
#     p_offset = []
#     p_duration = []
#     pr_differences = []
#
#     for n in range(len(p_loc)):
#         loc2 = p_loc[n] + window_size
#         p_start = r_peak_ecg_signal[loc2]
#         p_off = r_peak_ecg_signal[loc2 + 1]
#         count = 1
#
#         while p_start < p_off:
#             p_start = r_peak_ecg_signal[loc2 + count]
#             p_off = r_peak_ecg_signal[loc2 + 1 + count]
#             count = count + 1
#
#         p_offset.append(loc2 + count)
#         p_duration.append(qrs_onset[n] - (p_loc[n] - count))
#         pr_differences.append(qrs_onset[n] - (loc2 + count))
#
#     average_p_duration = np.mean(np.subtract(p_offset, p_loc))
#     average_pr = np.mean(pr_differences) + 80
#     if average_pr < 80:
#         average_pr = average_pr + 100
#     if average_pr < 0:
#         average_pr = average_pr + 150
#
#     return p_offset, p_duration, average_pr
#
#
# def detect_qrs_onset_offset(r_location, r_peak_ecg_signal, onset_window=50,
#                             offset_window=50):  ### onset offset change into 80 80 when bundle brunch block
#     qrs_onset = []
#     qrs_offset = []
#     signal_len = len(r_peak_ecg_signal)
#
#     for m in range(len(r_location)):
#         # QRS Onset
#         loc = r_location[m] - onset_window
#
#         if loc <= 0:
#
#             qrs_onset.append(0)
#         else:
#             R_peak = r_peak_ecg_signal[loc]
#             R_prev = r_peak_ecg_signal[loc - 1]
#             count = 1
#
#             while R_peak > R_prev and (loc - count - 1) >= 0:
#                 R_peak = r_peak_ecg_signal[loc - count]
#                 R_prev = r_peak_ecg_signal[loc - count - 1]
#                 count = count + 1
#
#             qrs_onset.append(max(0, loc - count))
#
#         # QRS Offset
#         loc2 = r_location[m] + offset_window
#
#         if loc2 >= signal_len - 1:
#             qrs_offset.append(signal_len - 1)
#         else:
#             R_on = r_peak_ecg_signal[loc2]
#             R_end = r_peak_ecg_signal[loc2 + 1]
#             count = 1
#
#             while R_on < R_end and (loc2 + count + 1) < signal_len:
#                 R_on = r_peak_ecg_signal[loc2 + count]
#                 R_end = r_peak_ecg_signal[loc2 + 1 + count]
#                 count = count + 1
#
#             qrs_offset.append(min(signal_len - 1, loc2 + count))
#
#     return qrs_onset, qrs_offset
#
#
# def calculate_pr_interval(r_location, qrs_onset, ecg_signal, offset_value=80):
#     r_loc_new = np.empty(len(r_location), dtype=int)
#     pr_array = np.empty(len(r_location))
#
#     for v in range(len(r_location)):
#         r_loc_new[v] = r_location[v] - offset_value
#         pr_array[v] = np.mean(ecg_signal[(r_loc_new[v]):(qrs_onset[v])])
#
#     return r_loc_new, pr_array
#
#
# def calculate_mode(pr_array):
#     mode_value = st.mode(pr_array)
#     return mode_value
#
#
# def calculate_new_qrs_onset_offset(r_location, r_peak_ecg_signal, mode_value, offset_before=50,
#                                    offset_after=50):  ###  offset before and offset after  make it 80 80 if bundle bunch block
#     new_qrs_onset = []
#     new_qrs_offset = []
#     signal_len = len(r_peak_ecg_signal)
#
#     for a in range(len(r_location)):
#         # QRS Onset
#         loc = r_location[a] - offset_before
#         if loc <= 0:
#             new_qrs_onset.append(0)
#         else:
#             R_peak = r_peak_ecg_signal[loc]
#             R_prev = r_peak_ecg_signal[loc - 1]
#             count = 1
#             while R_peak > R_prev > mode_value and (loc - count - 1) >= 0:  # Added boundary check
#                 R_peak = r_peak_ecg_signal[loc - count]
#                 R_prev = r_peak_ecg_signal[loc - count - 1]
#                 count = count + 1
#             new_qrs_onset.append(max(0, loc - count + 1))  # Adjusted for consistency
#
#     for b in range(len(r_location)):
#         # QRS Offset
#         loc2 = r_location[b] + offset_after
#         if loc2 >= signal_len - 1:  # If loc2 is already at or beyond the last valid index, the offset is the last index.
#             new_qrs_offset.append(signal_len - 1)
#         else:
#             # Check loc2 + 1 before accessing it in r_end
#             if loc2 + 1 >= signal_len:
#                 new_qrs_offset.append(
#                     signal_len - 1)  # If loc2 is valid but loc2+1 is not, offset is the last index
#             else:
#                 r_on = r_peak_ecg_signal[loc2]
#                 r_end = r_peak_ecg_signal[loc2 + 1]
#                 count = 1
#                 while r_on < r_end < mode_value and (
#                         loc2 + count + 1) < signal_len:  # Added boundary check
#                     r_on = r_peak_ecg_signal[loc2 + count]
#                     r_end = r_peak_ecg_signal[loc2 + 1 + count]
#                     count = count + 1
#                 new_qrs_offset.append(
#                     min(signal_len - 1, loc2 + count - 1))  # Adjusted for consistency
#
#     return new_qrs_onset, new_qrs_offset
#
#
# def R_Peak_Detection_05(raw_data):
#     peaks_up = [];
#     amp = [];
#     peaks_up_s = [];
#     ecp = [];
#     b = signal.firwin(1377, cutoff=[2 / 500], window="hamming", pass_zero=False);
#     a = 1
#     h, g = signal.butter(4, [45 / 500], btype='low', analog=False)
#     filt_BW = signal.filtfilt(b, a, raw_data);
#     data_F = signal.lfilter(h, g, filt_BW)
#     data_D = np.diff(data_F);
#     data_S = data_D * data_D
#
#     robust_peak_ref = np.percentile(data_S, 99.5)
#     peaks, rpeak = signal.find_peaks(data_S, distance=255, height=(robust_peak_ref / 1.7));
#     n = len(peaks);
#
#     for i in range(0, n):
#         win = 70;
#         if (peaks[i] - (win)) < 0:
#             start_win = 0
#         else:
#             start_win = (peaks[i] - (win))
#         seg = data_F[start_win:(peaks[i]) + (
#             win)];  # seg = seg*seg; ############## Multiple Seg for getting the R or S peak
#         max_peak, peak_amp = signal.find_peaks(seg, distance=10, height=(max(seg) / 2))
#         # FIX: find_peaks will not flag a maximum that sits exactly at
#         # the edge of `seg` as a peak, so max_peak can come back EMPTY
#         # (not just length 1) -- the original `len(max_peak) < 2` branch
#         # then did max_peak[0] on an empty array -> IndexError. Fall
#         # back to the raw argmax whenever find_peaks found nothing.
#         if len(max_peak) == 0:
#             peaks_u = start_win + int(np.argmax(seg))
#         elif len(max_peak) == 1:
#             peaks_u = start_win + max_peak[0]
#         else:
#             peaks_u = start_win + np.argmax(seg)
#         peaks_up.append(peaks_u);
#
#         seg_s = seg * (-1);
#         max_peak, peak_amp = signal.find_peaks(seg_s, distance=10)  # , height = (max(seg_s)/2))
#         # Same fix applied here for the S-wave search.
#         if len(max_peak) == 0:
#             peaks_u = start_win + int(np.argmax(seg_s))
#         elif len(max_peak) == 1:
#             peaks_u = start_win + max_peak[0]
#         else:
#             peaks_u = start_win + np.argmax(seg_s)
#         peaks_up_s.append(peaks_u);
#
#     rr_int = np.diff(peaks_up);
#     # FIX: statistics.mean() raises StatisticsError on an empty sequence,
#     # which happens whenever only 0 or 1 R-peaks were found in this
#     # buffer. Fail with a clear, catchable error instead of crashing.
#     if len(rr_int) == 0:
#         raise ValueError("Only one (or zero) R peak detected in this buffer — "
#                          "cannot compute RR statistics.")
#     th_ep = st.mean(rr_int) + st.mean(rr_int) / 10
#
#     for j in range(0, len(rr_int)):
#         if rr_int[j] > th_ep:
#             ep_seg = (data_F[peaks_up[j] + 60:peaks_up[j + 1] - 60])
#             max_e = np.argmax(ep_seg * ep_seg)  ########### for irregularity needs to be fixed
#             max_e = max_e + peaks_up[j] + 60;
#             # if ep_data[max_e] > (data_F[peaks_up[j]]/2):
#             ecp.append(max_e)
#     r_peak_values = [data_F[peak] for peak in peaks_up]
#     return peaks_up, ecp, peaks_up_s
#
#
# def P_Detection(rawECG, R_peaks, RRint, Ramp):
#     """
#     FIXED (2 bugs):
#
#     Bug A - crash ("list index out of range"):
#         The original k==0 branch, when the first R-peak sat closer than
#         80 samples from the start of the buffer, appended a single NaN
#         and then executed `break`. `break` exits the ENTIRE for-loop
#         over all beats, not just beat 0 - so `loc`/`Pamp`/`P_st`/`P_sp`
#         ended up EMPTY whenever that edge case hit. The later loop
#         `if ... Pamp[m] > ...` then indexed into an empty `Pamp` list
#         for m >= 0 and crashed with IndexError, which analyzelead4
#         caught and turned into an all-zero result.
#         FIX: use `continue` (skip only this beat) and use a -1 sentinel
#         for every array so each beat ALWAYS gets exactly one slot in
#         loc/Pamp/P_st/P_sp - keeping them the same length as `peaks`
#         and `RRint`, which every downstream index (m, candidate_idx)
#         assumes.
#
#     Bug B (latent): several np.where / window slices could come back
#         empty on degenerate windows (very short RR interval, exact
#         duplicate max value at the boundary, etc.), which would also
#         raise IndexError. Each of those spots now checks for an empty
#         result and skips that beat (sentinel -1) instead of crashing.
#
#     Note: `P_location = loc` (unfiltered) is intentional, not a bug -
#     compute_his_bundle_intervals() indexes P_onset/R_Location by the
#     SAME beat number (candidate_idx), so P_onset must have exactly one
#     entry per R-peak, in order, even for excluded/invalid beats.
#     """
#     Filt_ECG = ecg_filters_V5_smooth(rawECG)
#     peaks = R_peaks
#     sig_len = len(Filt_ECG)
#
#     PVC = find_PVC(Filt_ECG, peaks, Ramp, RRint)
#     PT_pwave = phasor_transform(ecg_filters_Pwave(rawECG), Rv=0.05)
#
#     P_st = []
#     P_sp = []
#     loc = []      # -1 sentinel = "no P wave found for this beat"
#     Pamp = []     # np.nan sentinel = "no P wave found for this beat"
#
#     n_peaks = len(peaks)
#
#     for k in range(n_peaks):
#         win_st = None
#         win_sp = None
#         skip = False
#
#         if k == 0:  # 1st P-peak detection
#             win_1 = peaks[0]
#             if win_1 > 300:
#                 win_st = peaks[0] - 300
#                 win_sp = peaks[0] - 80
#             elif 80 <= win_1 <= 300:
#                 win_st = 0
#                 win_sp = peaks[0] - 80
#             else:
#                 # Not enough samples before the first R-peak to search
#                 # for a P wave. FIX: skip only THIS beat (used to
#                 # `break` the whole loop here).
#                 skip = True
#         elif 0 < k < n_peaks - 1:
#             win_st = int(peaks[k] - (0.4 * RRint[k]))
#             win_sp = int(peaks[k] - (0.05 * (RRint[k] - 100)))
#         else:  # last peak
#             win_st = int(peaks[k] - (0.4 * RRint[k - 1]))
#             win_sp = int(peaks[k] - (0.05 * (RRint[k - 1] - 100)))
#
#         if not skip:
#             win_st = max(0, win_st)
#             win_sp = min(sig_len, win_sp)
#             if win_sp <= win_st:
#                 # Degenerate window (e.g. an unusually short RR interval
#                 # right after an ectopic beat) - nothing to search.
#                 skip = True
#
#         if not skip:
#             ch = np.max(PT_pwave[win_st:win_sp])
#             loc_ch = np.where(PT_pwave == ch)[0]
#             if len(loc_ch) == 0:
#                 skip = True
#
#         if not skip:
#             loc_ch0 = loc_ch[0]
#             loc_st = max(0, loc_ch0 - 80)
#             loc_sp = min(sig_len, loc_ch0 + 80)
#             if loc_sp <= loc_st:
#                 skip = True
#
#         if not skip:
#             ix = np.max(Filt_ECG[loc_st:loc_sp])
#             loc_ch2 = np.where(Filt_ECG == ix)[0]
#             if len(loc_ch2) == 0:
#                 skip = True
#
#         if skip:
#             loc.append(-1)
#             Pamp.append(np.nan)
#             P_st.append(win_st if win_st is not None else -1)
#             P_sp.append(win_sp if win_sp is not None else -1)
#             continue
#
#         final_loc = int(loc_ch2[0])
#         loc.append(final_loc)
#         Pamp.append(np.abs(Filt_ECG[final_loc]))
#         P_st.append(win_st)
#         P_sp.append(win_sp)
#
#     # loc/Pamp/P_st/P_sp now have exactly one entry per beat (n_peaks
#     # entries), so every array below stays index-aligned with `peaks`.
#
#     P_location = loc  # unfiltered on purpose - see docstring note above
#
#     P_onset = []
#     for m in range(len(P_location)):
#         p_loc = P_location[m]
#         if p_loc < 0:
#             P_onset.append(-1)
#             continue
#
#         # Walk backwards from the P peak looking for where the wave
#         # starts rising. FIX: bounded the walk so it can't index
#         # negative into the buffer (silent wraparound) or loop forever
#         # if T_on never dips below prev.
#         max_count = p_loc - 21
#         if max_count <= 0:
#             P_onset.append(max(0, p_loc - 1))
#             continue
#
#         T_on = Filt_ECG[p_loc]
#         prev = Filt_ECG[p_loc - 21]
#         Count = 1
#         while T_on > prev and Count < max_count:
#             T_on = Filt_ECG[p_loc - Count]
#             prev = Filt_ECG[p_loc - 20 - Count - 1]
#             Count = Count + 1
#         P_onset.append(p_loc - Count)
#
#     return (P_location, Pamp, P_onset)
#
#
# def ecg_filters_Twave(
#         lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     h, g = sp.signal.butter(4, [20 / 500], btype='low', analog=False)
#     j, i = sp.signal.butter(2, [15 / 500], btype='low', analog=False)
#     filt_50 = sp.signal.lfilter(d, c, lead_data);  # filt_100 = sp.signal.lfilter(f, e, filt_50)
#     filt_LP = sp.signal.lfilter(h, g, filt_50)
#     smoothed = sp.signal.savgol_filter(filt_LP, window_length=19, polyorder=3)
#     n = 41;
#     baseline_wander = sp.signal.medfilt(smoothed, kernel_size=n);
#     return baseline_wander
#
#
# def T_Detection(raw_signal, R_peaks, RRint):
#     Filt_ECG = ecg_filters_V5_smooth(raw_signal)
#     T_filt = ecg_filters_Twave(raw_signal)
#     PT_twave = phasor_transform(T_filt, Rv=0.1)
#     T_st = [];
#     T_sp = [];
#     T_loc = [];
#     Tamp = []
#     for k in range(0, len(R_peaks)):
#         if k == len(R_peaks) - 1:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k - 1]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k - 1])));  # print(win_st,win_sp)
#         elif k > 0 and k < len(R_peaks) - 1:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k])));  # print(win_st,win_sp)
#         else:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k + 1]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k + 1])));  # print(win_st,win_sp)
#         # Check if the slice is empty and handle it
#         if win_st >= win_sp:
#             print(f"Warning: Empty slice for R-peak index {k}. win_st: {win_st}, win_sp: {win_sp}")
#             # Skip this iteration if the slice is empty
#             continue
#         else:
#             ch = np.max(PT_twave[win_st:win_sp])
#         loc_ch = (np.where(PT_twave == ch));
#         loc_ch = loc_ch[0];
#         # print("window :",loc_ch[0]-80,loc_ch[0]+80)
#
#         # Check if loc_ch is empty before indexing
#         if len(loc_ch) > 0:
#             ix = np.max(Filt_ECG[loc_ch[0] - 80:loc_ch[0] + 80]);
#             loc_ch = (np.where(Filt_ECG == ix));
#             loc_ch = loc_ch[0];
#             T_loc.append(loc_ch[0]);
#             Tamp.append(np.abs(Filt_ECG[loc_ch[0]]))
#             T_st.append(win_st);
#             T_sp.append(win_sp)
#         else:
#             print(f"Warning: loc_ch is empty for R-peak index {k}. Skipping this iteration.")
#             # Handle the case where loc_ch is empty, e.g., append NaN to T_loc, etc.
#
#     return (np.array(T_loc))
#
#
# def calculate_t_offset(t_loc, r_peak_ecg_signal):
#     t_offset = []
#     signal_length = len(r_peak_ecg_signal)
#
#     for o in range(len(t_loc)):
#         location = t_loc[o]
#         t_start = r_peak_ecg_signal[location]
#         t_off = r_peak_ecg_signal[min(location + 1, signal_length - 1)]
#         count = 1
#
#         while t_start < t_off and location + count + 1 < signal_length:
#             t_start = r_peak_ecg_signal[min(location + count, signal_length - 1)]
#             t_off = r_peak_ecg_signal[min(location + count + 1, signal_length - 1)]
#             count += 1
#
#         # Ensure the offset doesn't go out of bounds
#         final_offset = min(location + count + 100, signal_length - 1)
#         t_offset.append(final_offset)
#
#     return t_offset
#
#
# def bandpass(signal_in, fs, lowcut=30, highcut=100, order=4):
#     nyq = 0.5 * fs
#     b, a = butter(order, [lowcut / nyq, highcut / nyq], btype='band')
#     return filtfilt(b, a, signal_in)
#
#
# def compute_his_bundle_intervals(v2, fs=1000, lsb_V=286e-9):
#     """
#     FIXED: previously this function computed R_Location TWICE — once from
#     the raw (unsmoothed) signal, and again from a smoothed signal — and
#     let the second call silently overwrite `R_Location`. That meant:
#       - QRS_Onset / New_QRS_Onset / New_QRS_Offset / P_onset were computed
#         against the FIRST (raw-signal) R_Location,
#       - but T_loc / T_Offset ended up computed against the SECOND
#         (smoothed-signal) R_Location, reusing a stale RRint from the
#         first pass,
#       - and later, `candidate_idx` was used to index into P_onset,
#         R_Location, New_QRS_Onset and New_QRS_Offset all at once,
#         assuming index i means "the same beat" in every array — which
#         was only true if raw-signal and smoothed-signal peak detection
#         happened to find the exact same number of beats at the exact
#         same locations. Any mismatch silently misaligned every landmark
#         used downstream (PA/AH/HV/PR/QRS/SNR all wrong).
#
#     FIX: detect R-peaks exactly once, from the raw signal, and keep
#     every downstream array (RRint, QRS_Onset/Offset, New_QRS_Onset/Offset,
#     P_location/P_onset, T_loc/T_Offset) anchored to that SAME R_Location.
#     T_Detection already does its own internal filtering, so we just feed
#     it a smoothed version of the raw signal for a cleaner T-wave search —
#     we do NOT re-run R-peak detection on it.
#     """
#
#     ###--------------------------- Raw_data fetching and R peak detection ----------------------------###
#     R_data = v2 * (-1)
#     R_peak_ecg_signal = ecg_filters_V5_01(R_data)
#     R_Location, ectopic, S_Location = R_Peak_Detection_05(R_data)
#     R_Location = np.array(R_Location, dtype=int)
#
#     if len(R_Location) < 3:
#         raise ValueError("Not enough R peaks detected in this buffer.")
#
#     ###--------------------------Heart Rate calculation---------------------------------- ###
#     R_amp = [];
#     RRint = [];
#     for i in range(0, len(R_Location) - 1):
#         ch = R_Location[i + 1] - R_Location[i];
#         RRint.append(ch)
#         R_amp.append(R_peak_ecg_signal[R_Location[i]])
#         if i == (len(R_Location) - 2):
#             R_amp.append(R_peak_ecg_signal[R_Location[i + 1]])
#
#     QRS_Onset, QRS_Offset = detect_qrs_onset_offset(R_Location, R_peak_ecg_signal)
#     R_Loc_New, PR_Array = calculate_pr_interval(R_Location, QRS_Onset, R_peak_ecg_signal)
#     Mode_Value = calculate_mode(PR_Array)
#     New_QRS_Onset, New_QRS_Offset = calculate_new_qrs_onset_offset(R_Location, R_peak_ecg_signal,
#                                                                    Mode_Value)
#     P_location, Pamp, P_onset = P_Detection(R_data, R_Location, RRint, R_amp)
#
#     # ---------------------------------------------------------------------
#     # T-wave detection.
#     # FIX: we no longer recompute R_Location / R_peak_ecg_signal here.
#     # We just hand T_Detection a smoothed version of the RAW (inverted)
#     # signal — T_Detection performs its own internal filtering pipeline
#     # (ecg_filters_V5_smooth + ecg_filters_Twave) on whatever it's given,
#     # so passing already-smoothed data double/triple-filters and shifts
#     # T_loc. Passing the same v2-derived raw signal keeps this consistent
#     # with how R_Location/RRint were computed above, and avoids the
#     # beat-count mismatch that broke candidate_idx alignment.
#     # ---------------------------------------------------------------------
#     T_loc = T_Detection(R_data, R_Location, RRint)
#     T_Offset = calculate_t_offset(T_loc, R_peak_ecg_signal)
#
#     T_Offset = np.array(T_Offset, dtype=int)
#     P_location = np.array(P_location, dtype=int)
#     P_onset = np.array(P_onset, dtype=int)
#
#     ads_values_tloc_p_location = []
#     for i, t_offset in enumerate(T_Offset):
#         next_p_onset = next((p for p in P_onset if p > t_offset), None)
#         if next_p_onset is not None and next_p_onset < len(R_peak_ecg_signal):
#             seg_start = int(t_offset)
#             seg_end = int(next_p_onset)
#             ads_segment = np.round(np.abs(R_peak_ecg_signal[seg_start:seg_end] / 0.000286)).astype(
#                 int)
#             ads_values_tloc_p_location.append(ads_segment)
#
#     if not ads_values_tloc_p_location:
#         raise ValueError("No T→P segments found for ADS analysis.")
#
#     # -------------------------------------------------------------------------
#     #                SELECT BEST 5 SEGMENTS (by RMS voltage)
#     # -------------------------------------------------------------------------
#     segment_scores = []
#     segment_indices = []
#
#     for i, ads_segment in enumerate(ads_values_tloc_p_location):
#         ads_segment = np.asarray(ads_segment, dtype=float)
#         if len(ads_segment) < 10:  # ignore very small segments
#             continue
#         voltage = ads_segment * lsb_V
#         rms = np.sqrt(np.mean(voltage ** 2))
#         segment_scores.append(rms)
#         segment_indices.append(i)
#
#     segment_scores = np.array(segment_scores)
#     segment_indices = np.array(segment_indices)
#
#     if len(segment_indices) == 0:
#         raise ValueError("No valid segments (all shorter than 10 samples) for ADS analysis.")
#
#     sorted_order = np.argsort(segment_scores)  # ascending: quietest first
#     candidate_indices = segment_indices[sorted_order]  # ALL candidates, best first
#
#     last_error = None
#     result_core = None
#
#     # FIX (AH sometimes reporting 0): _evaluate_candidate_beat used to
#     # silently fall back to H_on_i = A_wave_i whenever it couldn't find
#     # a genuine H-wave zero-crossing, which forces AH_ms = 0 on
#     # whichever candidate happened to be tried first - even though a
#     # later (still perfectly good) candidate beat might have had a real,
#     # correctly-located H-onset. That fallback made a "failure" look
#     # like a normal successful result, so the retry loop never got a
#     # chance to move on to a better beat.
#     #
#     # Two-pass fix: first pass over ALL candidates REQUIRES a genuine
#     # H-onset (strict=True) - only picks a beat where AH is real, not
#     # guessed. Only if every single candidate lacks a real H-onset do
#     # we fall back to a second pass that allows the A_wave_i guess, so
#     # we still return *something* rather than hard-failing the buffer.
#     for strict in (True, False):
#         for candidate_idx in candidate_indices:
#             try:
#                 result_core = _evaluate_candidate_beat(
#                     candidate_idx,
#                     P_onset=P_onset,
#                     R_Location=R_Location,
#                     New_QRS_Onset=New_QRS_Onset,
#                     New_QRS_Offset=New_QRS_Offset,
#                     R_peak_ecg_signal=R_peak_ecg_signal,
#                     fs=fs,
#                     strict_h_onset=strict,
#                 )
#                 break
#             except ValueError as e:
#                 last_error = e
#                 continue
#         if result_core is not None:
#             break
#
#     if result_core is None:
#         raise ValueError(
#             f"No usable beat found among {len(candidate_indices)} candidates "
#             f"(last error: {last_error})."
#         )
#
#     return result_core
#
#
# def _evaluate_candidate_beat(
#         candidate_idx,
#         P_onset,
#         R_Location,
#         New_QRS_Onset,
#         New_QRS_Offset,
#         R_peak_ecg_signal,
#         fs,
#         strict_h_onset=True,
# ):
#
#     if candidate_idx >= len(P_onset) or candidate_idx >= len(R_Location):
#         raise ValueError("Candidate segment index out of range for P_onset / R_Location.")
#
#     R_peak_i = int(R_Location[candidate_idx])
#
#     # ---------- QRS onset ----------
#     qrs_candidates = [q for q in New_QRS_Onset if q < R_peak_i]
#     if len(qrs_candidates) == 0:
#         raise ValueError("No QRS onset found before this candidate's R peak.")
#     QRS_on_i = qrs_candidates[-1]
#
#     # ---------- QRS offset ----------
#     qrs_off_candidates = [q for q in New_QRS_Offset if q > R_peak_i]
#     if len(qrs_off_candidates) == 0:
#         raise ValueError("No QRS offset found after this candidate's R peak.")
#     QRS_off_i = qrs_off_candidates[0]
#
#     # ---------- P onset for this beat ----------
#     p_on_i = int(P_onset[candidate_idx])
#     if p_on_i < 0:
#         # -1 sentinel from P_Detection: no P wave could be located for
#         # this beat, so there's nothing usable here.
#         raise ValueError("No P-onset detected for this candidate beat.")
#     if p_on_i >= QRS_on_i:
#         raise ValueError("P-onset is not before QRS-onset for this candidate beat.")
#
#     # ---------- Bandpass the P-onset -> QRS-onset segment ----------
#     seg_start = p_on_i
#     seg_end = QRS_on_i + 1
#     raw_segment = R_peak_ecg_signal[seg_start:seg_end]
#
#     if len(raw_segment) <= 10:
#         raise ValueError("P-onset to QRS-onset segment too short to bandpass filter.")
#
#     bp = bandpass(raw_segment, fs)
#
#     # ---------- SNR of this Segment ----------
#     # bp is the 30-100Hz bandpassed component (where the His-Purkinje
#     # deflection lives); raw_segment - bp is everything outside that
#     # band, treated as "noise" relative to the His signal.
#     #
#     # A true ratio is used here (not an artificially non-negative one),
#     # so a genuinely noise-dominant segment correctly reports a
#     # low/negative dB value instead of always looking fine.
#     signal_power = np.mean(bp ** 2)
#     noise = raw_segment - bp
#     noise_power = np.mean(noise ** 2)
#
#     eps = 1e-12
#     if noise_power > eps:
#         SNR_dB = 10 * np.log10(max(signal_power, eps) / noise_power)
#     else:
#         SNR_dB = float("inf")
#
#     # ---------- A wave (true peak in early PR region) ----------
#     qrs_local = QRS_on_i - seg_start
#     search_end = int(0.45 * qrs_local)
#     sub = bp[15:search_end]
#     if len(sub) == 0:
#         raise ValueError("A-wave search window is empty for this candidate beat.")
#     peak_idx = np.argmax(sub)
#     A_wave_i = seg_start + 15 + peak_idx
#
#     # ---------- H onset (biphasic-wave zero crossing before QRS) ----------
#     search_start = max(0, qrs_local - 60)
#     search_end = qrs_local - 5
#     sub2 = bp[search_start:search_end]
#
#     H_on_i = None
#     zc_idx = None
#     for i in range(1, len(sub2)):
#         if sub2[i - 1] < 0 and sub2[i] > 0:
#             zc_idx = i
#             break
#
#     if zc_idx is not None:
#         for i in range(zc_idx, 1, -1):
#             if abs(sub2[i] - sub2[i - 1]) < 0.001:
#                 H_on_i = seg_start + search_start + i
#                 break
#
#     if H_on_i is None:
#         # FIX: previously always fell back silently here, which forces
#         # AH_ms = 0 and hides the fact that this candidate beat simply
#         # didn't have a detectable H-wave. When strict_h_onset=True
#         # (first pass), raise instead so the caller's retry loop moves
#         # on to the next candidate beat, which may have a real
#         # H-onset. Only on the second, non-strict pass (i.e. no
#         # candidate anywhere had a real H-onset) do we fall back to
#         # the guess, so we still return a result instead of failing
#         # the whole buffer.
#         if strict_h_onset:
#             raise ValueError("No H-onset zero-crossing found for this candidate beat.")
#         H_on_i = A_wave_i
#
#     # ---------- Intervals ----------
#     PA_ms = A_wave_i - p_on_i
#     AH_ms = H_on_i - A_wave_i
#     HV_ms = QRS_on_i - H_on_i
#     PR_ms = QRS_on_i - p_on_i
#     QRS_ms = QRS_off_i - QRS_on_i
#
#     return {
#         "PA": float(PA_ms),
#         "AH": float(AH_ms),
#         "HV": float(HV_ms),
#         "PR": float(PR_ms),
#         "QRS": float(QRS_ms),
#         "SNR": float(SNR_dB),
#         # Landmark sample indices for the beat that was actually used,
#         # so a caller can plot exactly what was measured instead of
#         # just the raw trace. Not part of the original 6-value result,
#         # purely additive - existing callers that only read
#         # PA/AH/HV/PR/QRS/SNR are unaffected.
#         "R_peak_i": R_peak_i,
#         "p_on_i": p_on_i,
#         "A_wave_i": A_wave_i,
#         "H_on_i": H_on_i,
#         "QRS_on_i": QRS_on_i,
#         "QRS_off_i": QRS_off_i,
#         "seg_start": seg_start,
#         "seg_end": seg_end,
#     }
#
#
# def analyzelead4(samples, graph_path):
#     """
#     NOTE: the original file defined this function TWICE. The first
#     (older) definition used a simplified inline analysis and was
#     completely shadowed/dead code, since Python just keeps whichever
#     definition of a name is seen last. It has been removed here — only
#     this version (which shares the exact same real algorithm as
#     process() via compute_his_bundle_intervals()) is kept, so there's
#     no risk of silently editing dead code.
#     """
#     try:
#         import matplotlib
#         matplotlib.use("Agg")
#         import matplotlib.pyplot as plt
#
#         fs = 1000
#
#         # ----------------------------------
#         # Convert Java ArrayList to Python list
#         # ----------------------------------
#         try:
#             # Direct conversion from Java ArrayList to NumPy (faster)
#             size = samples.size()
#             lead4 = np.zeros(size, dtype=np.float64)
#             for i in range(size):
#                 lead4[i] = float(samples.get(i))
#         except Exception:
#             # Already Python list
#             lead4 = np.array([float(x) for x in samples], dtype=np.float64)
#
#         # Quick validation
#         if len(lead4) == 0:
#             return [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ""]
#
#         # ----------------------------------
#         # Convert Lead4 to V2 (mV)
#         # ----------------------------------
#         gain = 1
#         scale = 0.000286
#         v2 = ((lead4) * scale) * (-(gain))
#
#         # ----------------------------------
#         # Real interval detection (same algorithm process() uses)
#         # ----------------------------------
#         result = compute_his_bundle_intervals(v2, fs=fs)
#
#         # ----------------------------------
#         # Generate Graph
#         # ----------------------------------
#         # Two panels: (1) the full raw trace for context, and (2) a
#         # zoomed-in view of the actual beat that was measured, with the
#         # detected landmarks marked. The old graph only showed panel 1,
#         # which meant there was no way to visually confirm WHICH beat
#         # was used or WHERE the PA/AH/HV/PR/QRS boundaries were placed.
#         fig, axes = plt.subplots(2, 1, figsize=(12, 9))
#
#         axes[0].plot(v2, color='blue', linewidth=0.8)
#         axes[0].set_title('Full ECG Trace (Lead4 / V2)')
#         axes[0].set_xlabel('Sample')
#         axes[0].set_ylabel('Amplitude (mV)')
#         axes[0].grid(True)
#
#         margin = 100
#         zoom_start = max(0, result["seg_start"] - margin)
#         zoom_end = min(len(v2), result["QRS_off_i"] + margin)
#
#         if zoom_end > zoom_start:
#             x_zoom = np.arange(zoom_start, zoom_end)
#             axes[1].plot(x_zoom, v2[zoom_start:zoom_end], color='black', linewidth=1.0)
#
#             landmarks = [
#                 ('P-onset', result["p_on_i"], 'green'),
#                 ('A-wave', result["A_wave_i"], 'orange'),
#                 ('H-onset', result["H_on_i"], 'purple'),
#                 ('QRS-onset', result["QRS_on_i"], 'red'),
#                 ('QRS-offset', result["QRS_off_i"], 'red'),
#                 ('R-peak', result["R_peak_i"], 'blue'),
#             ]
#
#             y_top = np.max(v2[zoom_start:zoom_end])
#             for label, idx, color in landmarks:
#                 if zoom_start <= idx < zoom_end:
#                     axes[1].axvline(idx, color=color, linestyle='--', linewidth=1)
#                     axes[1].text(idx, y_top, label, rotation=90,
#                                  va='top', ha='right', fontsize=8, color=color)
#
#         axes[1].set_title('Best His Bundle Segment (Detected Landmarks)')
#         axes[1].set_xlabel('Sample')
#         axes[1].set_ylabel('Amplitude (mV)')
#         axes[1].grid(True)
#
#         plt.tight_layout()
#         plt.savefig(graph_path, dpi=150, bbox_inches='tight')
#         plt.close()
#
#         # ----------------------------------
#         # Return: [PA, AH, HV, PR, QRS, SNR, graph_path]
#         # ----------------------------------
#         return [
#             result["PA"],
#             result["AH"],
#             result["HV"],
#             result["PR"],
#             result["QRS"],
#             result["SNR"],
#             str(graph_path)
#         ]
#
#     except Exception as e:
#         import traceback
#         print("================================")
#         print("PYTHON ERROR in analyzelead4")
#         print(str(e))
#         traceback.print_exc()
#         print("================================")
#         return [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ""]
#
#
# def process(buffer2):
#     """
#     Console/debug entry point. Uses the exact same real algorithm
#     as analyzelead4() via compute_his_bundle_intervals(), then
#     prints a formatted table.
#     """
#     fs = 1000
#
#     v2 = np.array(buffer2, dtype=np.float64)
#     gain = 1
#     scale = 0.000286
#     # Convert to mV and invert
#     v2 = ((v2) * scale) * (-(gain));
#
#     result = compute_his_bundle_intervals(v2, fs=fs)
#
#     PA_ms = result["PA"]
#     AH_ms = result["AH"]
#     HV_ms = result["HV"]
#     PR_ms = result["PR"]
#     QRS_ms = result["QRS"]
#     SNR_dB = result["SNR"]
#
#     ### Display #####
#     df = pd.DataFrame(
#         [[int(PA_ms), int(AH_ms), int(HV_ms), int(PR_ms), int(QRS_ms)]],
#         columns=[
#             "PA (ms)",
#             "AH (ms)",
#             "HV (ms)",
#             "PR (ms)",
#             "QRS (ms)"
#         ]
#     )
#
#     # ANSI escape codes
#     BOLD = "\033[1m"
#     BLACK = "\033[30m"
#     RESET = "\033[0m"
#     RED = "\033[91m"
#     BLUE = "\033[94m"
#     print("\n")
#     print(BOLD + BLACK + "=" * 60 + RESET)
#     print(BOLD + BLACK + "           BEST  HIS BUNDLE SEGMENT" + RESET)
#     print(BOLD + BLACK + "=" * 60 + RESET)
#     print()
#     print(BOLD + df.to_string(index=False) + RESET)
#     print(BOLD + BLACK + "=" * 60 + RESET)
#     print()
#
#     snr_color = RED if SNR_dB < 6 else BLUE
#     print(f"{BOLD}{snr_color}SNR of Best Segment : {SNR_dB:.2f} dB{RESET}")
#     print(BOLD + BLACK + "=" * 60 + RESET)
#
#     return {
#         "PA": PA_ms,
#         "AH": AH_ms,
#         "HV": HV_ms,
#         "PR": PR_ms,
#         "QRS": QRS_ms,
#         "SNR": SNR_dB
#     }


# poulami final code
#
# # def analyzelead4(samples,graph_path):
# def analyzelead4(samples, graph_path):
#     try:
#         import matplotlib
#         matplotlib.use("Agg")
#         import matplotlib.pyplot as plt
#
#         fs = 1000
#
#         # ----------------------------------
#         # Convert Java ArrayList to Python list
#         # ----------------------------------
#         try:
#             # Direct conversion from Java ArrayList to NumPy (faster)
#             size = samples.size()
#             lead4 = np.zeros(size, dtype=np.float64)
#             for i in range(size):
#                 lead4[i] = float(samples.get(i))
#         except Exception:
#             # Already Python list
#             lead4 = np.array([float(x) for x in samples], dtype=np.float64)
#
#         # Quick validation
#         if len(lead4) == 0:
#             return [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ""]
#
#         # ----------------------------------
#         # Convert Lead4 to V2 (mV)
#         # ----------------------------------
#         gain = 1
#         scale = 0.000286
#         v2 = ((lead4) * scale) * (-(gain))
#
#         # ----------------------------------
#         # Real interval detection (same algorithm process() uses)
#         # ----------------------------------
#         result = compute_his_bundle_intervals(v2, fs=fs)
#
#
# def data_loss(input_signal):
#     for x in range(1, len(input_signal) - 1, 1):  #### data loss
#         if (input_signal[
#             x] == 0):  ## checks zero values in input signal and replaces with the mean of adjacent non zero values
#             input_signal[x] = st.mean([input_signal[x - 1], input_signal[x + 1]])
#     return (input_signal)
#
#     ## time stamp ##
#
#
# # def get_time_from_timestamp(
# #         timestamp):  ## converting unit timestamp to a readable date and time format
# #     read_able = datetime.datetime.fromtimestamp(timestamp)
# #     now_asia = str(read_able.astimezone(timezone('Asia/Kolkata')));  # print(now_asia)
# #     year = now_asia[0:10]
# #     year = year[8:10] + "-" + year[5:7] + "-" + year[0:4]
# #     time = now_asia[11:19]
# #     return (year, time)
# ## function extracts values from a dictionary given a specific index ##
#
#
# def Value_axis(data_dict, value):
#     data = list(data_dict.items())
#     an_array = np.array(data, dtype=object)
#     Values_1 = an_array[value]
#     Values_2 = np.asarray(Values_1[1])
#     return Values_2
#
#
# def ecg_filters_V5_smooth(lead_data, hp=0.67):
#     """
#     ECG filter according to IEC 60601-2-25
#     - Baseline wander removal
#     - Low-pass decomposition
#     - Smooth reconstruction
#     """
#
#     lead_data = np.asarray(lead_data, dtype=float).copy()
#
#     # Replace zero samples
#     for i in range(1, len(lead_data)):
#         if lead_data[i] == 0:
#             lead_data[i] = lead_data[i - 1]
#
#     # ------------------------------------------------------------------
#     # Baseline Wander Removal
#     # ------------------------------------------------------------------
#     b = signal.firwin(
#         2377,
#         cutoff=hp / 500,
#         window="hamming",
#         pass_zero=False
#     )
#
#     filt_BW = signal.filtfilt(b, [1], lead_data)
#
#     # ------------------------------------------------------------------
#     # Low-pass filters
#     # ------------------------------------------------------------------
#     lp45_b, lp45_a = signal.butter(
#         2,
#         45 / 500,
#         btype='low'
#     )
#
#     lp15_b, lp15_a = signal.butter(
#         2,
#         15 / 500,
#         btype='low'
#     )
#
#     filt_SM = signal.lfilter(lp15_b, lp15_a, filt_BW)
#     filt_LP = signal.lfilter(lp45_b, lp45_a, filt_BW)
#
#     # ------------------------------------------------------------------
#     # High-frequency component
#     # ------------------------------------------------------------------
#     sparse = filt_LP - filt_SM
#
#     # ------------------------------------------------------------------
#     # Replace TV denoising with Savitzky-Golay smoothing
#     # ------------------------------------------------------------------
#     denoise = signal.savgol_filter(
#         sparse,
#         window_length=29,
#         polyorder=3
#     )
#
#     # ------------------------------------------------------------------
#     # Reconstruct signal
#     # ------------------------------------------------------------------
#     smooth_data = filt_SM + denoise
#
#     return smooth_data
#
#
# def baseline_filter(lead_data, hp=0.67):
#     ##### Baseline filter without ST Segment distortion
#     b = signal.firwin(2377, cutoff=[hp / 500], window="hamming", pass_zero=False);
#     a = 1
#     filt_BW = signal.filtfilt(b, a, lead_data);
#     return filt_BW
#
#
# def ecg_filters_V5_01(lead_data):
#     """
#     ECG Filtering according to IEC 60601-2-25
#     - Baseline wander removal
#     - 50 Hz notch
#     - 100 Hz notch
#     - 45 Hz low-pass
#     - Optional Savitzky-Golay smoothing
#     """
#
#     lead_data = np.asarray(lead_data, dtype=float).copy()
#
#     # Replace missing (zero) samples
#     for i in range(1, len(lead_data)):
#         if lead_data[i] == 0:
#             lead_data[i] = lead_data[i - 1]
#
#     Fs = 1000.0
#
#     # ------------------------------------------------------------------
#     # Baseline Wander Removal (High-pass FIR)
#     # ------------------------------------------------------------------
#     hp_coeff = signal.firwin(
#         numtaps=2377,
#         cutoff=1,
#         fs=Fs,
#         pass_zero=False,
#         window="hamming"
#     )
#
#     baseline_removed = signal.filtfilt(hp_coeff, [1.0], lead_data)
#
#     # ------------------------------------------------------------------
#     # 50 Hz Notch Filter
#     # ------------------------------------------------------------------
#     notch50_b, notch50_a = signal.iirnotch(
#         w0=50,
#         Q=30,
#         fs=Fs
#     )
#
#     filtered = signal.filtfilt(notch50_b, notch50_a, baseline_removed)
#
#     # ------------------------------------------------------------------
#     # 100 Hz Notch Filter
#     # ------------------------------------------------------------------
#     notch100_b, notch100_a = signal.iirnotch(
#         w0=100,
#         Q=30,
#         fs=Fs
#     )
#
#     filtered = signal.filtfilt(notch100_b, notch100_a, filtered)
#
#     # ------------------------------------------------------------------
#     # 45 Hz Low-pass Butterworth
#     # ------------------------------------------------------------------
#     lp_b, lp_a = signal.butter(
#         N=4,
#         Wn=45,
#         btype='low',
#         fs=Fs
#     )
#
#     filtered = signal.filtfilt(lp_b, lp_a, filtered)
#
#     # ------------------------------------------------------------------
#     # Optional smoothing
#     # ------------------------------------------------------------------
#     filtered = signal.savgol_filter(
#         filtered,
#         window_length=29,
#         polyorder=3
#     )
#
#     return filtered
#
#
# def phasor_transform(signal, Rv):
#     PT = np.empty_like(signal, dtype=float)
#     for i in range(len(signal)):
#         ch = signal[i] / Rv
#         PT[i] = math.degrees(math.atan(ch))
#         # PT[i] = Rv+ ()
#     return (PT)
#
#
# def find_PVC(signal, Rpeaks, Ramplitued, RRinterval):
#     ##   LIST OF AREA UNDER THE CURVE
#     AUC = collections.deque()
#     for k in Rpeaks:
#         ch = abs(np.trapezoid(signal[k - 80:k + 80]));
#         AUC.append(ch)
#         # print("median : ",ch, 1.3*st.median(AUC))
#     AUC = np.array(AUC);  # print(len(AUC),AUC)
#     ##    ECTOPIC FUNCTION
#     PVC_list = np.empty_like(Rpeaks, dtype=float);
#     PVC_list[:] = np.nan
#     SVC_list = np.empty_like(Rpeaks, dtype=float);
#     SVC_list[:] = np.nan
#     for i in range(1, len(RRinterval)):
#         if RRinterval[i] >= (1.5 * RRinterval[i - 1]):
#             if Ramplitued[i] >= (Ramplitued[i - 1] + (0.2 * Ramplitued[i - 1])):  # PVC
#                 if AUC[i] > (1.3 * st.median(AUC[0:i - 1])):
#                     PVC_list[i] = Rpeaks[i]
#             else:  # SVC or PVC
#                 if i > 1:
#                     if AUC[i] > (1.3 * st.median(AUC[0:i - 1])):
#                         PVC_list[i] = Rpeaks[i]
#     return np.array(PVC_list)
#
#
# def ecg_filters_Pwave(
#         lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     h, g = sp.signal.butter(4, [40 / 500], btype='low', analog=False);
#     j, i = signal.butter(2, [15 / 500], btype='low', analog=False)
#     filt_50 = sp.signal.lfilter(d, c, lead_data);
#     filt_100 = sp.signal.lfilter(f, e, filt_50)
#     filt_LP = sp.signal.lfilter(h, g, filt_50)
#     smoothed = sp.signal.savgol_filter(filt_LP, window_length=19, polyorder=3)
#     n = 41;
#     baseline_wander = sp.signal.medfilt(smoothed, kernel_size=n);
#     return baseline_wander
#
#
# def calculate_p_offset_p_duration_and_average_pr(p_loc, r_peak_ecg_signal, qrs_onset,
#                                                  window_size=60):
#     p_offset = []
#     p_duration = []
#     pr_differences = []
#
#     for n in range(len(p_loc)):
#         loc2 = p_loc[n] + window_size
#         p_start = r_peak_ecg_signal[loc2]
#         p_off = r_peak_ecg_signal[loc2 + 1]
#         count = 1
#
#         while p_start < p_off:
#             p_start = r_peak_ecg_signal[loc2 + count]
#             p_off = r_peak_ecg_signal[loc2 + 1 + count]
#             count = count + 1
#
#         p_offset.append(loc2 + count)
#         p_duration.append(qrs_onset[n] - (p_loc[n] - count))
#         pr_differences.append(qrs_onset[n] - (loc2 + count))
#
#     average_p_duration = np.mean(np.subtract(p_offset, p_loc))
#     average_pr = np.mean(pr_differences) + 80
#     if average_pr < 80:
#         average_pr = average_pr + 100
#     if average_pr < 0:
#         average_pr = average_pr + 150
#     return p_offset, p_duration, average_pr
#
#
# def detect_qrs_onset_offset(r_location, r_peak_ecg_signal, onset_window=50,
#                             offset_window=50):  ### onset offset change into 80 80 when bundle brunch block
#     qrs_onset = []
#     qrs_offset = []
#
#     for m in range(len(r_location)):
#         # QRS Onset
#         loc = r_location[m] - onset_window
#         R_peak = r_peak_ecg_signal[loc]
#         R_prev = r_peak_ecg_signal[loc - 1]
#         count = 1
#
#         while R_peak > R_prev:
#             R_peak = r_peak_ecg_signal[loc - count]
#             R_prev = r_peak_ecg_signal[loc - count - 1]
#             count = count + 1
#
#         qrs_onset.append(loc - count)
#
#         # QRS Offset
#         loc2 = r_location[m] + offset_window
#         R_on = r_peak_ecg_signal[loc2]
#         R_end = r_peak_ecg_signal[loc2 + 1]
#         count = 1
#
#         while R_on < R_end:
#             R_on = r_peak_ecg_signal[loc2 + count]
#             R_end = r_peak_ecg_signal[loc2 + 1 + count]
#             count = count + 1
#
#         qrs_offset.append(loc2 + count)
#
#     return qrs_onset, qrs_offset
#
#
# def calculate_pr_interval(r_location, qrs_onset, ecg_signal, offset_value=80):
#     r_loc_new = np.empty(len(r_location), dtype=int)
#     pr_array = np.empty(len(r_location))
#
#     for v in range(len(r_location)):
#         r_loc_new[v] = r_location[v] - offset_value
#         pr_array[v] = np.mean(ecg_signal[(r_loc_new[v]):(qrs_onset[v])])
#
#     return r_loc_new, pr_array
#
#
# def calculate_mode(pr_array):
#     mode_value = st.mode(pr_array)
#     return mode_value
#
#
# def calculate_new_qrs_onset_offset(r_location, r_peak_ecg_signal, mode_value, offset_before=50,
#                                    offset_after=50):  ###  offset before and offset after  make it 80 80 if bundle bunch block
#     new_qrs_onset = []
#     new_qrs_offset = []
#     signal_len = len(r_peak_ecg_signal)
#
#     for a in range(len(r_location)):
#         # QRS Onset
#         loc = r_location[a] - offset_before
#         if loc <= 0:
#             new_qrs_onset.append(0)
#         else:
#             R_peak = r_peak_ecg_signal[loc]
#             R_prev = r_peak_ecg_signal[loc - 1]
#             count = 1
#             while R_peak > R_prev > mode_value and (
#                     loc - count - 1) >= 0:  # Added boundary check
#                 R_peak = r_peak_ecg_signal[loc - count]
#                 R_prev = r_peak_ecg_signal[loc - count - 1]
#                 count = count + 1
#             new_qrs_onset.append(max(0, loc - count + 1))  # Adjusted for consistency
#
#     for b in range(len(r_location)):
#         # QRS Offset
#         loc2 = r_location[b] + offset_after
#         if loc2 >= signal_len - 1:  # If loc2 is already at or beyond the last valid index, the offset is the last index.
#             new_qrs_offset.append(signal_len - 1)
#         else:
#             # Check loc2 + 1 before accessing it in r_end
#             if loc2 + 1 >= signal_len:
#                 new_qrs_offset.append(
#                     signal_len - 1)  # If loc2 is valid but loc2+1 is not, offset is the last index
#             else:
#                 r_on = r_peak_ecg_signal[loc2]
#                 r_end = r_peak_ecg_signal[loc2 + 1]
#                 count = 1
#                 while r_on < r_end < mode_value and (
#                         loc2 + count + 1) < signal_len:  # Added boundary check
#                     r_on = r_peak_ecg_signal[loc2 + count]
#                     r_end = r_peak_ecg_signal[loc2 + 1 + count]
#                     count = count + 1
#                 new_qrs_offset.append(
#                     min(signal_len - 1, loc2 + count - 1))  # Adjusted for consistency
#
#     return new_qrs_onset, new_qrs_offset
#
#
# def R_Peak_Detection_05(raw_data):
#     peaks_up = [];
#     amp = [];
#     peaks_up_s = [];
#     ecp = [];
#     b = signal.firwin(1377, cutoff=[2 / 500], window="hamming", pass_zero=False);
#     a = 1
#     h, g = signal.butter(4, [45 / 500], btype='low', analog=False)
#     filt_BW = signal.filtfilt(b, a, raw_data);
#     data_F = signal.lfilter(h, g, filt_BW)
#     data_D = np.diff(data_F);
#     data_S = data_D * data_D
#     peaks, rpeak = signal.find_peaks(data_S, distance=255,
#                                      height=(max(data_S[1000:5000]) / 1.7));
#     n = len(peaks);
#
#     for i in range(0, n):
#         win = 70;
#         if (peaks[i] - (win)) < 0:
#             start_win = 0
#         else:
#             start_win = (peaks[i] - (win))
#         seg = data_F[start_win:(peaks[i]) + (
#             win)];  # seg = seg*seg; ############## Multiple Seg for getting the R or S peak
#         max_peak, peak_amp = signal.find_peaks(seg, distance=10, height=(max(seg) / 2))
#         if len(max_peak) < 2:
#             peaks_u = start_win + max_peak[0]
#         else:
#             peaks_u = start_win + np.argmax(seg)
#         peaks_up.append(peaks_u);
#
#         seg_s = seg * (-1);
#         max_peak, peak_amp = signal.find_peaks(seg_s, distance=10)  # , height = (max(seg_s)/2))
#         if len(max_peak) < 2:
#             peaks_u = start_win + max_peak[0]  ########### need fix for short S wave
#         else:
#             peaks_u = start_win + np.argmax(seg_s)
#         peaks_up_s.append(peaks_u);
#
#     rr_int = np.diff(peaks_up);
#     th_ep = st.mean(rr_int) + st.mean(rr_int) / 10
#
#     for j in range(0, len(rr_int)):
#         if rr_int[j] > th_ep:
#             ep_seg = (data_F[peaks_up[j] + 60:peaks_up[j + 1] - 60])
#             max_e = np.argmax(ep_seg * ep_seg)  ########### for irregularity needs to be fixed
#             max_e = max_e + peaks_up[j] + 60;
#             # if ep_data[max_e] > (data_F[peaks_up[j]]/2):
#             ecp.append(max_e)
#     r_peak_values = [data_F[peak] for peak in peaks_up]
#     return peaks_up, ecp, peaks_up_s
#
#
# def P_Detection(rawECG, R_peaks, RRint, Ramp):
#     Filt_ECG = ecg_filters_V5_smooth(rawECG)
#     peaks = R_peaks;
#
#     PVC = find_PVC(Filt_ECG, peaks, Ramp, RRint)
#     PT_pwave = phasor_transform(ecg_filters_Pwave(rawECG), Rv=0.05)
#     P_st = [];
#     P_sp = [];
#     loc = [];
#     Pamp = []
#     P_location = [];
#     Pst = [];
#     Psp = []
#     for k in range(0, len(peaks)):
#         if k == 0:  # 1st P-peak detection
#             win_1 = len(Filt_ECG[0:peaks[0]]);  # print(win_1)
#             if win_1 > 300:
#                 win_st = peaks[0] - 300;
#                 win_sp = peaks[0] - 80
#                 ch = np.max(PT_pwave[peaks[0] - 300:peaks[0] - 80]);
#             elif win_1 <= 300 and win_1 >= 80:
#                 win_st = 0;
#                 win_sp = peaks[0] - 80
#                 ch = np.max(PT_pwave[0:peaks[0] - 80]);
#             # elif win_1<80:
#             #   P_location.append(np.nan) win_st = np.nan win_sp = np.nan
#             #   break;
#
#             elif win_1 < 80:
#                 loc.append(np.nan)
#                 Pamp.append(np.nan)
#                 P_st.append(np.nan)
#                 P_sp.append(np.nan)
#                 continue
#         elif k > 0 and k < len(peaks) - 1:  # Second to Last but 1 peak
#             win_st = int(peaks[k] - (0.4 * RRint[k]));
#             win_sp = int(peaks[k] - (0.05 * (RRint[k] - 100)));  # print(win_st,win_sp)
#             ch = np.max(PT_pwave[win_st:win_sp])
#         else:  # Last peak
#             win_st = int(peaks[k] - (0.4 * RRint[k - 1]));
#             win_sp = int(peaks[k] - (0.05 * (RRint[k - 1] - 100)));  # print(win_st,win_sp)
#             ch = np.max(PT_pwave[win_st:win_sp])
#         loc_ch = (np.where(PT_pwave == ch));
#         loc_ch = loc_ch[0];
#         if loc_ch[0] < 80:
#             loc_st = loc_ch[0]
#         else:
#             loc_st = loc_ch[0] - 80
#         ix = np.max(Filt_ECG[loc_st:loc_ch[0] + 80]);
#         loc_ch = (np.where(Filt_ECG == ix));
#         loc_ch = loc_ch[0];
#         loc.append(loc_ch[0]);
#         Pamp.append(np.abs(Filt_ECG[loc_ch[0]]))
#         P_st.append(win_st);
#         P_sp.append(win_sp)
#
#     # print("Pamp temp :", len(Pamp), Pamp);  print("Ploc temp :", len(loc), loc)
#     N = min(len(RRint),
#             len(PVC),
#             len(Pamp),
#             len(loc),
#             len(P_st),
#             len(P_sp),
#             len(peaks))
#     for m in range(0, len(N)):
#         # print(m)
#         if np.isnan(PVC[m]) == True and Pamp[m] > (
#                 0.5 * Filt_ECG[peaks[m]]):  # Beat Not a PVC: retain P loc
#             P_location.append(loc[m])
#             Pst.append(P_st[m]);
#             Psp.append(P_sp[m])
#             '''if m>1 and (RRint[m]>(1.6*RRint[m-1])):    #Beat not 1st and RR > 1.6*prevRR :: check dissosiated P wave
#               win_st = int(peaks[m]+400); win_sp = int(loc[m+1]-400);
#               print(m, win_st, win_sp)
#               ch = np.max(PT_pwave[win_sp:win_st]); loc_ch = (np.where(PT_pwave==ch)); loc_ch = loc_ch[0];
#               ix = np.max(Filt_ECG[loc_ch[0]-80:loc_ch[0]+80]); loc_ch = (np.where(Filt_ECG==ix)); loc_ch = loc_ch[0];
#               amp = np.abs(Filt_ECG[loc_ch[0]])
#               if amp>(0.05*Filt_ECG[peaks[m]]):    #Pamp>0.05*QRS amp: Dissosiated P detected
#                 P_location.append(loc_ch[0]); Pamp = np.insert(Pamp, m, (Filt_ECG[loc_ch[0]]))
#                 Pst.append(win_st); Psp.append(P_sp[m]))'''
#
#     P_location = loc
#     P_onset = [];  # print("P location : ",P_location)
#     # print("Pst :", Pst); print("Psp :",Psp)
#     for m in range(len(P_location)):
#         loc = P_location[m]
#         T_on = Filt_ECG[loc - 0];
#         prev = Filt_ECG[loc - 20 - 1];
#         Count = 1
#         while T_on > prev:
#             T_on = Filt_ECG[loc - 0 - Count];
#             prev = Filt_ECG[loc - 20 - Count - 1];
#             Count = Count + 1
#         P_onset.append(loc - Count)
#     return (P_location, Pamp, P_onset)
#
#
# def ecg_filters_Twave(
#         lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     h, g = sp.signal.butter(4, [20 / 500], btype='low', analog=False)
#     j, i = sp.signal.butter(2, [15 / 500], btype='low', analog=False)
#     filt_50 = sp.signal.lfilter(d, c, lead_data);  # filt_100 = sp.signal.lfilter(f, e, filt_50)
#     filt_LP = sp.signal.lfilter(h, g, filt_50)
#     smoothed = sp.signal.savgol_filter(filt_LP, window_length=19, polyorder=3)
#     n = 41;
#     baseline_wander = sp.signal.medfilt(smoothed, kernel_size=n);
#     return baseline_wander
#
#
# def T_Detection(raw_signal, R_peaks, RRint):
#     Filt_ECG = ecg_filters_V5_smooth(raw_signal)
#     T_filt = ecg_filters_Twave(raw_signal)
#     PT_twave = phasor_transform(T_filt, Rv=0.1)
#     T_st = [];
#     T_sp = [];
#     T_loc = [];
#     Tamp = []
#     for k in range(0, len(R_peaks)):
#         if k == len(R_peaks) - 1:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k - 1]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k - 1])));  # print(win_st,win_sp)
#         elif k > 0 and k < len(R_peaks) - 1:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k])));  # print(win_st,win_sp)
#         else:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k + 1]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k + 1])));  # print(win_st,win_sp)
#         # Check if the slice is empty and handle it
#         if win_st >= win_sp:
#             print(
#                 f"Warning: Empty slice for R-peak index {k}. win_st: {win_st}, win_sp: {win_sp}")
#             # Skip this iteration if the slice is empty
#             continue
#         else:
#             ch = np.max(PT_twave[win_st:win_sp])
#         loc_ch = (np.where(PT_twave == ch));
#         loc_ch = loc_ch[0];
#         # print("window :",loc_ch[0]-80,loc_ch[0]+80)
#
#         # Check if loc_ch is empty before indexing
#         if len(loc_ch) > 0:
#             ix = np.max(Filt_ECG[loc_ch[0] - 80:loc_ch[0] + 80]);
#             loc_ch = (np.where(Filt_ECG == ix));
#             loc_ch = loc_ch[0];
#             T_loc.append(loc_ch[0]);
#             Tamp.append(np.abs(Filt_ECG[loc_ch[0]]))
#             T_st.append(win_st);
#             T_sp.append(win_sp)
#         else:
#             print(f"Warning: loc_ch is empty for R-peak index {k}. Skipping this iteration.")
#             # Handle the case where loc_ch is empty, e.g., append NaN to T_loc, etc.
#
#     return (np.array(T_loc))
#
#
# def calculate_t_offset(t_loc, r_peak_ecg_signal):
#     t_offset = []
#     signal_length = len(r_peak_ecg_signal)
#
#     for o in range(len(t_loc)):
#         location = t_loc[o]
#         t_start = r_peak_ecg_signal[location]
#         t_off = r_peak_ecg_signal[min(location + 1, signal_length - 1)]
#         count = 1
#
#         while t_start < t_off and location + count + 1 < signal_length:
#             t_start = r_peak_ecg_signal[min(location + count, signal_length - 1)]
#             t_off = r_peak_ecg_signal[min(location + count + 1, signal_length - 1)]
#             count += 1
#
#         # Ensure the offset doesn't go out of bounds
#         final_offset = min(location + count + 100, signal_length - 1)
#         t_offset.append(final_offset)
#
#     return t_offset
#
#
# # ==========================================================
# #               SNR CALCULATION
# # ==========================================================
#
# def calculate_snr_db(raw_signal, filtered_signal):
#     raw_signal = np.asarray(raw_signal, dtype=float)
#     filtered_signal = np.asarray(filtered_signal, dtype=float)
#
#     noise = raw_signal - filtered_signal
#
#     signal_power = np.mean(filtered_signal ** 2)
#     noise_power = np.mean(noise ** 2)
#
#     if noise_power <= 1e-20:
#         return 99.0
#
#     larger = max(signal_power, noise_power)
#     smaller = max(min(signal_power, noise_power), 1e-20)
#
#     return 10 * np.log10(larger / smaller)
#
#
# fs = 1000
# lsb_V = 286e-9
#
# # ----------------------------------
# # Convert Java ArrayList to Python list
# # ----------------------------------
# try:
#     # Direct conversion from Java ArrayList to NumPy (faster)
#     size = samples.size()
#     lead4 = np.zeros(size, dtype=np.float64)
#     for i in range(size):
#         lead4[i] = float(samples.get(i))
# except Exception:
#     # Already Python list
#     lead4 = np.array([float(x) for x in samples], dtype=np.float64)
#
# # Quick validation
# if len(lead4) == 0:
#     print("ERROR: Empty input data")
#     return [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ""]
#
# # Check for valid data (not all zeros)
# if np.all(lead4 == 0):
#     print("ERROR: All zeros in input data")
#     return [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ""]
#
# # ----------------------------------
# # Convert Lead4 to V2 (mV)
# # ----------------------------------
# gain = 1
# scale = 0.000286
#
# ###-----------------Starting Point of ECG Segment------------------------##
#
# #  buffer = client.fetch(macId, ts)
# #  frame = buffer.frame
# # Extract leads
# #  Lead1 = frame.i; Lead2 = frame.ii; v1 = frame.v1; v2 = frame.v2;v3 = frame.v3;v4 = frame.v4;v5 = frame.v5;v6 = frame.v6  # for adding V6 enter V5 here
# #  gain = 1
# #  # Convert to mV and invert
# #  Lead1 = ((Lead1) * 0.000286) * (-(gain));Lead2 = ((Lead2) * 0.000286) * (-(gain));v1 = ((v1) * 0.000286) * (-(gain));v2 = ((v2) * 0.000286) * (-(gain));v3 = ((v3) * 0.000286) * (-(gain));v4 = ((v4) * 0.000286) * (-(gain));v5 = ((v5) * 0.000286) * (-(gain));v6 = ((v6) * 0.000286) * (-(gain))
# #  scale = 0.000286
#
# lead4 = np.asarray(samples, dtype=np.float64)
# v2 = ((lead4) * scale) * (-(gain))
# R_data = v2 * (-1)
# R_peak_ecg_signal = ecg_filters_V5_01(R_data)
# R_Location, ectopic, S_Location = R_Peak_Detection_05(R_data)
# R_Location = np.array(R_Location, dtype=int)
#
# ###--------------------------Heart Rate calculation---------------------------------- ###
# R_amp = [];
# RRint = [];
# for i in range(0, len(R_Location) - 1):
#     ch = R_Location[i + 1] - R_Location[i];
#     RRint.append(ch)
#     R_amp.append(R_peak_ecg_signal[R_Location[i]])
#     if i == (len(R_Location) - 2):
#         R_amp.append(R_peak_ecg_signal[R_Location[i + 1]])
#
# QRS_Onset, QRS_Offset = detect_qrs_onset_offset(R_Location, R_peak_ecg_signal)
# R_Loc_New, PR_Array = calculate_pr_interval(R_Location, QRS_Onset, R_peak_ecg_signal)
# distance_between_QRS_on_R_peak = np.subtract(R_Location, QRS_Onset)
# Mode_Value = calculate_mode(PR_Array)
# New_QRS_Onset, New_QRS_Offset = calculate_new_qrs_onset_offset(R_Location, R_peak_ecg_signal,
#                                                                Mode_Value)
# new_QRS_difference = np.subtract(New_QRS_Offset, New_QRS_Onset)
# new_average_QRS = np.mean(new_QRS_difference)
# PVC = find_PVC(R_data, R_Location, R_amp, RRint)  #### not added
# P_location, Pamp, P_onset = P_Detection(R_data, R_Location, RRint, R_amp)
# P_Offset, P_Duration, Average_PR = calculate_p_offset_p_duration_and_average_pr(P_location,
#                                                                                 R_data,
#                                                                                 New_QRS_Onset)  #### not added
#
# filtered_v2 = ecg_filters_V5_smooth(v2)
#
# R_data = filtered_v2 * (-1)
# # -------------------------------------------------------
# #           SNR OF CURRENT TIMESTAMP
# # -------------------------------------------------------
#
# raw_signal = v2 * (-1)
# filtered_signal = R_data
# SNR_dB = calculate_snr_db(raw_signal, filtered_signal)
# R_peak_ecg_signal = ecg_filters_V5_smooth(R_data)
# R_Location, ectopic, S_Location = R_Peak_Detection_05(R_data)
# T_loc = T_Detection(R_peak_ecg_signal, R_Location, RRint)
# T_Offset = calculate_t_offset(T_loc, R_peak_ecg_signal)
# valid_indices = [int(idx) for idx in T_Offset if idx < len(R_peak_ecg_signal)]  #### not added
# # Convert to arrays
# T_Offset = np.array(T_Offset, dtype=int)
# P_location = np.array(P_location, dtype=int)
# P_onset = np.array(P_onset, dtype=int)
# ads_values_tloc_p_location = []
# for i, t_offset in enumerate(T_Offset):
#     # Find the next valid P_onset that occurs AFTER this T_offset
#     next_p_onset = next((p for p in P_onset if p > t_offset), None)
#
#     if next_p_onset is not None and next_p_onset < len(R_peak_ecg_signal):
#         seg_start = int(t_offset)
#         seg_end = int(next_p_onset)
#
#         # Extract ADS segment
#         ads_segment = np.round(np.abs(R_peak_ecg_signal[seg_start:seg_end] / 0.000286)).astype(
#             int)
#         ads_values_tloc_p_location.append(ads_segment)
#
# segment_max_V = []
# segment_max_mV = []
#
# for i, ads_segment in enumerate(ads_values_tloc_p_location):
#     ads_segment = np.asarray(ads_segment, dtype=float)
#     voltage_V = ads_segment * lsb_V
#     max_v_V = np.max(voltage_V)
#     max_v_mV = max_v_V * 1e3
#     segment_max_V.append(max_v_V)
#     segment_max_mV.append(max_v_mV)
#
# segment_max_V = np.array(segment_max_V)
# segment_max_mV = np.array(segment_max_mV)
# # -------------------------------------------------------------------------
# #                SELECT BEST 5 SEGMENTS
# # -------------------------------------------------------------------------
#
# segment_scores = []
# segment_indices = []
#
# for i, ads_segment in enumerate(ads_values_tloc_p_location):
#     ads_segment = np.asarray(ads_segment, dtype=float)
#
#     if len(ads_segment) < 10:  # ignore very small segments
#         continue
#
#     # Convert to voltage
#     voltage = ads_segment * lsb_V
#
#     # Score = RMS (robust metric)
#     rms = np.sqrt(np.mean(voltage ** 2))
#
#     segment_scores.append(rms)
#     segment_indices.append(i)
#
# # Convert to numpy
# segment_scores = np.array(segment_scores)
# segment_indices = np.array(segment_indices)
#
# # Get top 5 indices (sorted descending)
# top5_idx_sorted = segment_indices[np.argsort(segment_scores)[-5:]][::-1]
#
# for i, t_offset in enumerate(T_Offset):
#     next_p_onset = next((p for p in P_onset if p > t_offset), None)
#
#     if next_p_onset is not None:
#         if i in top5_idx_sorted:
#
#             seg_start = int(t_offset)
#             seg_end = int(next_p_onset)
#
#             # -----------------------------------------
#             # FIND CORRESPONDING QRS ONSET
#             # -----------------------------------------
#             qrs_candidates = [q for q in New_QRS_Onset if q < seg_start]
#
#             if len(qrs_candidates) > 0:
#                 qrs_onset = qrs_candidates[-1]  # nearest previous QRS onset
#
# for rank, idx in enumerate(top5_idx_sorted):
#
#     if idx >= len(P_onset) or idx >= len(P_location):
#         continue
#
#     p_on = int(P_onset[idx])
#     p_peak = int(P_location[idx])
#
#     # Get amplitude values
#     p_on_val = R_peak_ecg_signal[p_on]
#     p_peak_val = R_peak_ecg_signal[p_peak]
#
# best_idx = top5_idx_sorted[0]  # BEST segment
# R_peak = int(R_Location[best_idx])
# R_peak_initial = int(R_Location[best_idx])
# qrs_candidates = [q for q in New_QRS_Onset if q < R_peak]
# # -----------------------------------------
# # Refine QRS onset to the knee (slope change)
# # -----------------------------------------
# search_left = max(0, R_peak - 80)
# search_right = R_peak
# segment = R_peak_ecg_signal[search_left:search_right]
# d1 = np.diff(segment)
# d2 = np.diff(d1)
# # slope threshold
# slope_thr = 0.20 * np.max(d1)
# QRS_on = search_left
#
# # Search backwards from R peak
# for i in range(len(d1) - 5, 5, -1):
#
#     # look for beginning of rapid upstroke
#     if d1[i] < slope_thr and np.mean(d1[i + 1:i + 6]) > slope_thr:
#         QRS_on = search_left + i
#         break
#
# # ----------------------------
# # QRS OFFSET
# # ----------------------------
# qrs_offset_candidates = [q for q in New_QRS_Offset if q > R_peak]
#
# if len(qrs_offset_candidates) > 0:
#     QRS_off = qrs_offset_candidates[0]  # nearest offset after R peak
# else:
#     QRS_off = None
#
# # Define small correction window
# search_left = max(0, R_peak_initial - 20)
# search_right = min(len(R_peak_ecg_signal), R_peak_initial + 20)
# # Find true local maximum
# local_segment = R_peak_ecg_signal[search_left:search_right]
# local_max_idx = np.argmax(local_segment)
# # Corrected R peak
# R_peak = search_left + local_max_idx
# local_max_idx = np.argmax(np.abs(local_segment))
#
# # -------------------------------------------------------
# #        DEFINE WINDOW (R \u00b1 400)
# # -------------------------------------------------------
# left_window = 400
# right_window = 400
# start = max(0, R_peak - left_window)
# end = min(len(R_peak_ecg_signal), R_peak + right_window)
# beat_signal = R_peak_ecg_signal[start:end]
# x_axis = np.arange(start, end)
#
# # P onset (only if inside window)
# if best_idx < len(P_onset):
#     p_on = int(P_onset[best_idx])
#
# # T offset (only if inside window)
# if best_idx < len(T_Offset):
#     t_off = int(T_Offset[best_idx])
#
# filtered_segment = None
# if QRS_on is not None and best_idx < len(P_onset):
#
#     p_on = int(P_onset[best_idx])
#
#     if p_on < QRS_on:
#
#         seg_start = p_on
#         seg_end = QRS_on + 1  # IMPORTANT: include QRS_on
#
#         raw_segment = R_peak_ecg_signal[seg_start:seg_end]
#         seg_x = np.arange(seg_start, seg_end)
#
#         # -------------------------------------------------------
#         #        BANDPASS FILTER (30\u201380 Hz)
#         # -------------------------------------------------------
#         from scipy.signal import butter, filtfilt
#
#
#         def bandpass_filter(signal, fs, lowcut=30, highcut=100, order=4):
#             nyq = 0.5 * fs
#             low = lowcut / nyq
#             high = highcut / nyq
#             b, a = butter(order, [low, high], btype='band')
#             return filtfilt(b, a, signal)
#
#
#         if len(raw_segment) > 10:
#             filtered_segment = bandpass_filter(raw_segment, fs)
#
#         if QRS_on is not None and best_idx < len(P_onset):
#
#             p_on = int(P_onset[best_idx])
#
#             if p_on < QRS_on:
#
#                 seg_start = p_on
#                 seg_end = QRS_on + 1
#
#                 raw_segment = R_peak_ecg_signal[seg_start:seg_end]
#                 seg_x = np.arange(seg_start, seg_end)
#
#                 # -------------------------------
#                 # BANDPASS (30\u2013100 Hz)
#                 # -------------------------------
#                 from scipy.signal import butter, filtfilt
#
#
#                 def bandpass(signal, fs, lowcut=30, highcut=100, order=4):
#                     nyq = 0.5 * fs
#                     b, a = butter(order, [lowcut / nyq, highcut / nyq], btype='band')
#                     return filtfilt(b, a, signal)
#
#
#                 if len(raw_segment) > 10:
#                     bp = bandpass(raw_segment, fs)
#
#                     # Normalize + scale for visibility
#                     bp_norm = bp / np.max(np.abs(bp))
#                     scale = 0.5 * np.max(np.abs(raw_segment))
#                     bp_scaled = bp_norm * scale
#
# # -------------------------------------------------------
# #        A WAVE DETECTION (FINAL - TRUE PEAK, NO SHIFT)
# # -------------------------------------------------------
# A_wave = None
# bp_segment = bp
# qrs_local = QRS_on - seg_start
# # Early PR region (tight window)
# search_start = 15
# search_end = int(0.45 * qrs_local)
# sub = bp_segment[search_start:search_end]
# if len(sub) > 5:
#     peak_idx = np.argmax(sub)  # TRUE peak (not first, not thresholded)
#     A_wave = seg_start + search_start + peak_idx
#
# # -------------------------------------------------------
# #        H_on and H_Off DETECTION (TRUE BIPHASIC START)
# # -------------------------------------------------------
#
# H_on = None  ### replace in main code  ####
# bp_segment = bp  # bandpassed signal
# qrs_local = QRS_on - seg_start
# # search region before QRS (where your circled wave is)
# search_start = max(0, qrs_local - 60)
# search_end = qrs_local - 5
# sub = bp_segment[search_start:search_end]
# # Step 1: find zero crossing (center of biphasic)
# zc_idx = None
# for i in range(1, len(sub)):
#     if sub[i - 1] < 0 and sub[i] > 0:
#         zc_idx = i
#         break
#
# # Step 2: go BACK to find beginning of that wave
# if zc_idx is not None:
#     for i in range(zc_idx, 1, -1):
#         # beginning = where slope starts increasing
#         if abs(sub[i] - sub[i - 1]) < 0.001:
#             H_on = seg_start + search_start + i
#             break
#
# # search between H_on and QRS
# if H_on is not None:
#     h_on_local = H_on - seg_start
#
#     search_start = h_on_local + 5
#     search_end = qrs_local - 2
#
#     sub = bp_segment[search_start:search_end]
#
#     if len(sub) > 5:
#         # find LAST zero-crossing before QRS
#         for i in range(len(sub) - 1, 1, -1):
#             if sub[i - 1] > 0 and sub[i] < 0:
#                 H_off = seg_start + search_start + i
#                 break  ### till here ###
#
# # ==========================================================
# #           TABLE
# # ==========================================================
# table_rows = []
#
# # ---------- Best segment ----------
# p_on_i = int(P_onset[best_idx])
# R_peak_i = int(R_Location[best_idx])
#
# # ---------- QRS onset ----------
# qrs_candidates = [q for q in New_QRS_Onset if q < R_peak_i]
# QRS_on_i = qrs_candidates[-1]
#
# # ---------- QRS offset ----------
# qrs_off_candidates = [q for q in New_QRS_Offset if q > R_peak_i]
# QRS_off_i = qrs_off_candidates[0]
#
# # ---------- Bandpass ----------
# seg_start = p_on_i
# seg_end = QRS_on_i + 1
#
# raw_segment = R_peak_ecg_signal[seg_start:seg_end]
# bp = bandpass(raw_segment, fs)
#
# # ---------- A wave ----------
# qrs_local = QRS_on_i - seg_start
#
# sub = bp[15:int(0.45 * qrs_local)]
# peak_idx = np.argmax(sub)
# A_wave_i = seg_start + 15 + peak_idx
#
# # ---------- H onset ----------
# search_start = max(0, qrs_local - 60)
# search_end = qrs_local - 5
#
# sub2 = bp[search_start:search_end]
#
# H_on_i = None
#
# zc_idx = None
# for i in range(1, len(sub2)):
#     if sub2[i - 1] < 0 and sub2[i] > 0:
#         zc_idx = i
#         break
#
# if zc_idx is not None:
#     for i in range(zc_idx, 1, -1):
#         if abs(sub2[i] - sub2[i - 1]) < 0.001:
#             H_on_i = seg_start + search_start + i
#             break
#
# # ---------- Intervals ----------
# PA_ms = A_wave_i - p_on_i
#
# if H_on_i is not None:
#     AH_ms = H_on_i - A_wave_i
#     HV_ms = QRS_on_i - H_on_i
# else:
#     AH_ms = np.nan
#     HV_ms = np.nan
#
# PR_ms = QRS_on_i - p_on_i
# QRS_ms = QRS_off_i - QRS_on_i
#
# table_rows.append([
#     int(PA_ms),
#     int(AH_ms) if not np.isnan(AH_ms) else np.nan,
#     int(HV_ms) if not np.isnan(HV_ms) else np.nan,
#     int(PR_ms),
#     int(QRS_ms)
# ])
#
# ### Display #####
# df = pd.DataFrame(
#     table_rows,
#     columns=[
#         "PA (ms)",
#         "AH (ms)",
#         "HV (ms)",
#         "PR (ms)",
#         "QRS (ms)"
#     ]
# )
# print(df.to_string(index=False))
#
# return {
#     "PA": int(PA_ms),
#     "AH": int(AH_ms),
#     "HV": int(HV_ms),
#     "PR": int(PR_ms),
#     "QRS": int(QRS_ms),
#     "SNR": float(SNR_dB)
# }
## ============================================================
## FIXED VERSION 2
## Changes in this revision:
##   1. HH value removed everywhere (no longer computed, no
##      longer returned to Android, no longer in the debug table).
##   2. THE MAIN BUG: analyzelead4() -- the function Android
##      actually calls -- was NOT using the real PA/AH/HV
##      detection algorithm. It was using placeholder guesses:
##          AH_ms = PR_ms * 0.3
##          HV_ms = QRS_ms * 0.2
##      These are not measurements, just arbitrary fractions of
##      PR/QRS, which is why the on-device numbers were wrong.
##      The *real* algorithm (bandpass filter -> A-wave peak ->
##      H-on/H-off zero-crossing detection) only existed inside
##      process(), which the app never called.
##   3. Fix: the real algorithm from process() has been pulled
##      out into one shared function,
##      compute_his_bundle_intervals(), and BOTH process() and
##      analyzelead4() now call it. So the values Android
##      receives are the same real measurement, not a guess.
##   4. No thresholds, window sizes, filter cutoffs, or any other
##      numeric parameter/logic used by the real algorithm were
##      changed -- only removed the fake approximations and wired
##      up the real one instead.
##   5. R_Peak_Detection_05: fixed IndexError crash.
##      "len(max_peak) < 2" matched BOTH zero peaks found AND one
##      peak found, then unconditionally indexed max_peak[0] --
##      which throws IndexError on an empty array. find_peaks()
##      requires a true local max with lower neighbors on both
##      sides, so a flat/noisy 140-sample window can legitimately
##      return zero peaks. Changed to "== 1" so only the true
##      single-peak case indexes max_peak[0]; zero-peak and
##      multi-peak cases both fall back to argmax(seg), same as
##      the pre-existing multi-peak fallback. Applied to both the
##      R-peak block and the mirrored S-wave block.
##   6. R_Peak_Detection_05: replaced the hardcoded threshold
##      window max(data_S[1000:5000]) with a whole-buffer robust
##      percentile (99.5th) so short buffers, or buffers where
##      the first ~4s happens to be noisy/quiet, don't miscalibrate
##      the peak-detection threshold for the entire buffer.
## ============================================================
#
# import math
# import collections
# import datetime
# import numpy as np
# import scipy as sp
# from scipy import signal
# from scipy.signal import butter, filtfilt
# import statistics as st
# import pandas as pd
# from pytz import timezone
#
# try:
#     import condat_tv
# except ImportError:
#     condat_tv = None
#     print("WARNING: condat_tv not available on this device — "
#           "sparse/TV denoise step will be skipped for this run.")
#
#
# ## data loss correction ##
#
# def data_loss(input_signal):
#     for x in range(1, len(input_signal) - 1, 1):  #### data loss
#         if (input_signal[
#             x] == 0):  ## checks zero values in input signal and replaces with the mean of adjacent non zero values
#             input_signal[x] = st.mean([input_signal[x - 1], input_signal[x + 1]])
#     return (input_signal)
#
#
# ## time stamp ##
# def get_time_from_timestamp(
#         timestamp):  ## converting unit timestamp to a readable date and time format
#     read_able = datetime.datetime.fromtimestamp(timestamp)
#     now_asia = str(read_able.astimezone(timezone('Asia/Kolkata')));  # print(now_asia)
#     year = now_asia[0:10]
#     year = year[8:10] + "-" + year[5:7] + "-" + year[0:4]
#     time = now_asia[11:19]
#     return (year, time)
#
#
# ## function extracts values from a dictionary given a specific index ##
# def Value_axis(data_dict, value):
#     data = list(data_dict.items())
#     an_array = np.array(data, dtype=object)
#     Values_1 = an_array[value]
#     Values_2 = np.asarray(Values_1[1])
#     return Values_2
#
#
# ## smoothening filter: Baseline filter+powerline noise+150 hz low pass filter ##
# def ecg_filters_V5_smooth(lead_data,
#                           hp=0.67):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     for x in range(1, len(lead_data), 1):
#         if (lead_data[x] == 0):
#             lead_data[x] = lead_data[x - 1]
#
#     ##### Baseline filter without ST Segment distortion
#     b = signal.firwin(2377, cutoff=[hp / 500], window="hamming", pass_zero=False);
#     a = 1
#     # b = signal.firwin(1735, cutoff = [0.67/500], window = "hamming", pass_zero=False); a = 1
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     # h, g = signal.butter(2, [45/500] , btype='low', analog=False)
#     h, g = signal.butter(2, [45 / 500], btype='low', analog=False)
#     j, i = signal.butter(2, [15 / 500], btype='low', analog=False)
#
#     filt_BW = signal.filtfilt(b, a, lead_data);
#     filt_SM = (signal.lfilter(j, i, filt_BW))
#     filt_LP = (signal.lfilter(h, g, filt_BW))
#
#     sparse = filt_LP - filt_SM;
#     if condat_tv is not None:
#         denoise = condat_tv.tv_denoise(sparse, 6.5)
#     else:
#         denoise = sparse  # fallback if the native lib isn't bundled on device
#     smooth_data = filt_SM + denoise
#
#     return smooth_data
#
#
# def baseline_filter(lead_data, hp=0.67):
#     ##### Baseline filter without ST Segment distortion
#     b = signal.firwin(2377, cutoff=[hp / 500], window="hamming", pass_zero=False);
#     a = 1
#     filt_BW = signal.filtfilt(b, a, lead_data);
#     return filt_BW
#
#
# def ecg_filters_V5_01(
#         lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     for x in range(1, len(lead_data), 1):
#         if (lead_data[x] == 0):
#             lead_data[x] = lead_data[x - 1]
#
#     ##### Baseline filter without ST Segment distortion
#     b = signal.firwin(2377, cutoff=[1 / 500], window="hamming", pass_zero=False);
#     a = 1
#     # b = signal.firwin(1735, cutoff = [0.67/500], window = "hamming", pass_zero=False); a = 1
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     h, g = signal.butter(4, [45 / 500], btype='low', analog=False)
#
#     filt_BW = signal.filtfilt(b, a, lead_data)
#     filt_50 = signal.lfilter(d, c, filt_BW);
#     filt_100 = signal.lfilter(f, e, filt_50)
#     filt_LP = signal.lfilter(h, g, filt_50)
#     smoothed = signal.savgol_filter(filt_LP, window_length=29, polyorder=3)
#     return filt_LP
#
#
# def phasor_transform(signal_in, Rv):
#     PT = np.empty_like(signal_in, dtype=float)
#     for i in range(len(signal_in)):
#         ch = signal_in[i] / Rv
#         PT[i] = math.degrees(math.atan(ch))
#         # PT[i] = Rv+ ()
#     return (PT)
#
#
# def find_PVC(signal_in, Rpeaks, Ramplitued, RRinterval):
#     ##   LIST OF AREA UNDER THE CURVE
#     AUC = collections.deque()
#     n_sig = len(signal_in)
#     for k in Rpeaks:
#         # Guard against negative slice start wrapping around to the END
#         # of the array (Python silently allows signal_in[-5:10], which
#         # silently corrupts the AUC/PVC score for early beats instead
#         # of raising an error).
#         lo = max(0, k - 80)
#         hi = min(n_sig, k + 80)
#         ch = abs(np.trapz(signal_in[lo:hi]));
#         AUC.append(ch)
#         # print("median : ",ch, 1.3*st.median(AUC))
#     AUC = np.array(AUC);  # print(len(AUC),AUC)
#     ##    ECTOPIC FUNCTION
#     PVC_list = np.empty_like(Rpeaks, dtype=float);
#     PVC_list[:] = np.nan
#     SVC_list = np.empty_like(Rpeaks, dtype=float);
#     SVC_list[:] = np.nan
#     for i in range(1, len(RRinterval)):
#         if RRinterval[i] >= (1.5 * RRinterval[i - 1]):
#             if Ramplitued[i] >= (Ramplitued[i - 1] + (0.2 * Ramplitued[i - 1])):  # PVC
#                 # Guard: at i == 1, AUC[0:i-1] == AUC[0:0] == empty,
#                 # and statistics.median() raises StatisticsError on an
#                 # empty sequence. There's no preceding-beat baseline to
#                 # compare against yet, so skip the PVC check for the
#                 # very first comparable beat instead of crashing the
#                 # whole buffer's analysis.
#                 if i > 1 and AUC[i] > (1.3 * st.median(AUC[0:i - 1])):
#                     PVC_list[i] = Rpeaks[i]
#             else:  # SVC or PVC
#                 if i > 1:
#                     if AUC[i] > (1.3 * st.median(AUC[0:i - 1])):
#                         PVC_list[i] = Rpeaks[i]
#     return np.array(PVC_list)
#
#
# def ecg_filters_Pwave(
#         lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     h, g = sp.signal.butter(4, [40 / 500], btype='low', analog=False);
#     j, i = signal.butter(2, [15 / 500], btype='low', analog=False)
#     filt_50 = sp.signal.lfilter(d, c, lead_data);
#     filt_100 = sp.signal.lfilter(f, e, filt_50)
#     filt_LP = sp.signal.lfilter(h, g, filt_50)
#     smoothed = sp.signal.savgol_filter(filt_LP, window_length=19, polyorder=3)
#     n = 41;
#     baseline_wander = sp.signal.medfilt(smoothed, kernel_size=n);
#     return baseline_wander
#
#
# def calculate_p_offset_p_duration_and_average_pr(p_loc, r_peak_ecg_signal, qrs_onset,
#                                                  window_size=60):
#     p_offset = []
#     p_duration = []
#     pr_differences = []
#
#     for n in range(len(p_loc)):
#         loc2 = p_loc[n] + window_size
#         p_start = r_peak_ecg_signal[loc2]
#         p_off = r_peak_ecg_signal[loc2 + 1]
#         count = 1
#
#         while p_start < p_off:
#             p_start = r_peak_ecg_signal[loc2 + count]
#             p_off = r_peak_ecg_signal[loc2 + 1 + count]
#             count = count + 1
#
#         p_offset.append(loc2 + count)
#         p_duration.append(qrs_onset[n] - (p_loc[n] - count))
#         pr_differences.append(qrs_onset[n] - (loc2 + count))
#
#     average_p_duration = np.mean(np.subtract(p_offset, p_loc))
#     average_pr = np.mean(pr_differences) + 80
#     if average_pr < 80:
#         average_pr = average_pr + 100
#     if average_pr < 0:
#         average_pr = average_pr + 150
#
#     return p_offset, p_duration, average_pr
#
#
# def detect_qrs_onset_offset(r_location, r_peak_ecg_signal, onset_window=50,
#                             offset_window=50):  ### onset offset change into 80 80 when bundle brunch block
#     qrs_onset = []
#     qrs_offset = []
#     signal_len = len(r_peak_ecg_signal)
#
#     for m in range(len(r_location)):
#         # QRS Onset
#         loc = r_location[m] - onset_window
#
#         if loc <= 0:
#             # Guard: negative loc would silently wrap around and index
#             # from the END of the array instead of erroring, corrupting
#             # this beat (and anything downstream, e.g. Mode_Value).
#             qrs_onset.append(0)
#         else:
#             R_peak = r_peak_ecg_signal[loc]
#             R_prev = r_peak_ecg_signal[loc - 1]
#             count = 1
#
#             while R_peak > R_prev and (loc - count - 1) >= 0:
#                 R_peak = r_peak_ecg_signal[loc - count]
#                 R_prev = r_peak_ecg_signal[loc - count - 1]
#                 count = count + 1
#
#             qrs_onset.append(max(0, loc - count))
#
#         # QRS Offset
#         loc2 = r_location[m] + offset_window
#
#         if loc2 >= signal_len - 1:
#             qrs_offset.append(signal_len - 1)
#         else:
#             R_on = r_peak_ecg_signal[loc2]
#             R_end = r_peak_ecg_signal[loc2 + 1]
#             count = 1
#
#             while R_on < R_end and (loc2 + count + 1) < signal_len:
#                 R_on = r_peak_ecg_signal[loc2 + count]
#                 R_end = r_peak_ecg_signal[loc2 + 1 + count]
#                 count = count + 1
#
#             qrs_offset.append(min(signal_len - 1, loc2 + count))
#
#     return qrs_onset, qrs_offset
#
#
# def calculate_pr_interval(r_location, qrs_onset, ecg_signal, offset_value=80):
#     r_loc_new = np.empty(len(r_location), dtype=int)
#     pr_array = np.empty(len(r_location))
#
#     for v in range(len(r_location)):
#         r_loc_new[v] = r_location[v] - offset_value
#         pr_array[v] = np.mean(ecg_signal[(r_loc_new[v]):(qrs_onset[v])])
#
#     return r_loc_new, pr_array
#
#
# def calculate_mode(pr_array):
#     mode_value = st.mode(pr_array)
#     return mode_value
#
#
# def calculate_new_qrs_onset_offset(r_location, r_peak_ecg_signal, mode_value, offset_before=50,
#                                    offset_after=50):  ###  offset before and offset after  make it 80 80 if bundle bunch block
#     new_qrs_onset = []
#     new_qrs_offset = []
#     signal_len = len(r_peak_ecg_signal)
#
#     for a in range(len(r_location)):
#         # QRS Onset
#         loc = r_location[a] - offset_before
#         if loc <= 0:
#             new_qrs_onset.append(0)
#         else:
#             R_peak = r_peak_ecg_signal[loc]
#             R_prev = r_peak_ecg_signal[loc - 1]
#             count = 1
#             while R_peak > R_prev > mode_value and (loc - count - 1) >= 0:  # Added boundary check
#                 R_peak = r_peak_ecg_signal[loc - count]
#                 R_prev = r_peak_ecg_signal[loc - count - 1]
#                 count = count + 1
#             new_qrs_onset.append(max(0, loc - count + 1))  # Adjusted for consistency
#
#     for b in range(len(r_location)):
#         # QRS Offset
#         loc2 = r_location[b] + offset_after
#         if loc2 >= signal_len - 1:  # If loc2 is already at or beyond the last valid index, the offset is the last index.
#             new_qrs_offset.append(signal_len - 1)
#         else:
#             # Check loc2 + 1 before accessing it in r_end
#             if loc2 + 1 >= signal_len:
#                 new_qrs_offset.append(
#                     signal_len - 1)  # If loc2 is valid but loc2+1 is not, offset is the last index
#             else:
#                 r_on = r_peak_ecg_signal[loc2]
#                 r_end = r_peak_ecg_signal[loc2 + 1]
#                 count = 1
#                 while r_on < r_end < mode_value and (
#                         loc2 + count + 1) < signal_len:  # Added boundary check
#                     r_on = r_peak_ecg_signal[loc2 + count]
#                     r_end = r_peak_ecg_signal[loc2 + 1 + count]
#                     count = count + 1
#                 new_qrs_offset.append(
#                     min(signal_len - 1, loc2 + count - 1))  # Adjusted for consistency
#
#     return new_qrs_onset, new_qrs_offset
#
#
# def R_Peak_Detection_05(raw_data):
#     peaks_up = [];
#     amp = [];
#     peaks_up_s = [];
#     ecp = [];
#     b = signal.firwin(1377, cutoff=[2 / 500], window="hamming", pass_zero=False);
#     a = 1
#     h, g = signal.butter(4, [45 / 500], btype='low', analog=False)
#     filt_BW = signal.filtfilt(b, a, raw_data);
#     data_F = signal.lfilter(h, g, filt_BW)
#     data_D = np.diff(data_F);
#     data_S = data_D * data_D
#     # Robust whole-buffer threshold instead of a hardcoded [1000:5000]
#     # window. The old fixed window assumed the first ~4 seconds of the
#     # buffer were always representative of QRS amplitude - if that
#     # window happened to contain noise/motion artifact (or was unusually
#     # quiet) while the rest of the buffer was clean, the threshold was
#     # miscalibrated for the ENTIRE buffer. A high percentile over the
#     # whole signal is robust to a few extreme edge-filter-transient
#     # samples while still scaling with genuine QRS amplitude, and works
#     # regardless of buffer length (1s, 15s, or otherwise).
#     robust_peak_ref = np.percentile(data_S, 99.5)
#     peaks, rpeak = signal.find_peaks(data_S, distance=255, height=(robust_peak_ref / 1.7));
#     n = len(peaks);
#
#     for i in range(0, n):
#         win = 70;
#         if (peaks[i] - (win)) < 0:
#             start_win = 0
#         else:
#             start_win = (peaks[i] - (win))
#         seg = data_F[start_win:(peaks[i]) + (
#             win)];  # seg = seg*seg; ############## Multiple Seg for getting the R or S peak
#         max_peak, peak_amp = signal.find_peaks(seg, distance=10, height=(max(seg) / 2))
#         # NOTE: "len(max_peak) < 2" also matches ZERO peaks found, and
#         # max_peak[0] then crashes with IndexError on an empty array.
#         # find_peaks() requires a true local max with lower neighbors on
#         # both sides, so a peak sitting right at the edge of this window
#         # (or a flat/noisy segment) can legitimately return zero peaks.
#         # Only trust max_peak[0] when exactly one peak was found; zero
#         # or multiple peaks both fall back to a plain argmax of the
#         # segment, same as the pre-existing multi-peak fallback below.
#         if len(max_peak) == 1:
#             peaks_u = start_win + max_peak[0]
#         else:
#             peaks_u = start_win + np.argmax(seg)
#         peaks_up.append(peaks_u);
#
#         seg_s = seg * (-1);
#         max_peak, peak_amp = signal.find_peaks(seg_s, distance=10)  # , height = (max(seg_s)/2))
#         if len(max_peak) == 1:
#             peaks_u = start_win + max_peak[0]  ########### need fix for short S wave
#         else:
#             peaks_u = start_win + np.argmax(seg_s)
#         peaks_up_s.append(peaks_u);
#
#     rr_int = np.diff(peaks_up);
#     th_ep = st.mean(rr_int) + st.mean(rr_int) / 10
#
#     for j in range(0, len(rr_int)):
#         if rr_int[j] > th_ep:
#             ep_seg = (data_F[peaks_up[j] + 60:peaks_up[j + 1] - 60])
#             max_e = np.argmax(ep_seg * ep_seg)  ########### for irregularity needs to be fixed
#             max_e = max_e + peaks_up[j] + 60;
#             # if ep_data[max_e] > (data_F[peaks_up[j]]/2):
#             ecp.append(max_e)
#     r_peak_values = [data_F[peak] for peak in peaks_up]
#     return peaks_up, ecp, peaks_up_s
#
#
# def P_Detection(rawECG, R_peaks, RRint, Ramp):
#     Filt_ECG = ecg_filters_V5_smooth(rawECG)
#     peaks = R_peaks;
#
#     PVC = find_PVC(Filt_ECG, peaks, Ramp, RRint)
#     PT_pwave = phasor_transform(ecg_filters_Pwave(rawECG), Rv=0.05)
#     P_st = [];
#     P_sp = [];
#     loc = [];
#     Pamp = []
#     P_location = [];
#     Pst = [];
#     Psp = []
#     for k in range(0, len(peaks)):
#         if k == 0:  # 1st P-peak detection
#             win_1 = len(Filt_ECG[0:peaks[0]]);  # print(win_1)
#             if win_1 > 300:
#                 win_st = peaks[0] - 300;
#                 win_sp = peaks[0] - 80
#                 ch = np.max(PT_pwave[peaks[0] - 300:peaks[0] - 80]);
#             elif win_1 <= 300 and win_1 >= 80:
#                 win_st = 0;
#                 win_sp = peaks[0] - 80
#                 ch = np.max(PT_pwave[0:peaks[0] - 80]);
#             elif win_1 < 80:
#                 P_location.append(np.nan)
#                 win_st = np.nan
#                 win_sp = np.nan
#                 break;
#         elif k > 0 and k < len(peaks) - 1:  # Second to Last but 1 peak
#             win_st = int(peaks[k] - (0.4 * RRint[k]));
#             win_sp = int(peaks[k] - (0.05 * (RRint[k] - 100)));  # print(win_st,win_sp)
#             ch = np.max(PT_pwave[win_st:win_sp])
#         else:  # Last peak
#             win_st = int(peaks[k] - (0.4 * RRint[k - 1]));
#             win_sp = int(peaks[k] - (0.05 * (RRint[k - 1] - 100)));  # print(win_st,win_sp)
#             ch = np.max(PT_pwave[win_st:win_sp])
#         loc_ch = (np.where(PT_pwave == ch));
#         loc_ch = loc_ch[0];
#         if loc_ch[0] < 80:
#             loc_st = loc_ch[0]
#         else:
#             loc_st = loc_ch[0] - 80
#         ix = np.max(Filt_ECG[loc_st:loc_ch[0] + 80]);
#         loc_ch = (np.where(Filt_ECG == ix));
#         loc_ch = loc_ch[0];
#         loc.append(loc_ch[0]);
#         Pamp.append(np.abs(Filt_ECG[loc_ch[0]]))
#         P_st.append(win_st);
#         P_sp.append(win_sp)
#
#     # print("Pamp temp :", len(Pamp), Pamp);  print("Ploc temp :", len(loc), loc)
#
#     for m in range(0, len(RRint)):
#         # print(m)
#         if np.isnan(PVC[m]) == True and Pamp[m] > (
#                 0.5 * Filt_ECG[peaks[m]]):  # Beat Not a PVC: retain P loc
#             P_location.append(loc[m])
#             Pst.append(P_st[m]);
#             Psp.append(P_sp[m])
#
#     P_location = loc
#     P_onset = [];  # print("P location : ",P_location)
#     # print("Pst :", Pst); print("Psp :",Psp)
#     for m in range(len(P_location)):
#         loc = P_location[m]
#         T_on = Filt_ECG[loc - 0];
#         prev = Filt_ECG[loc - 20 - 1];
#         Count = 1
#         while T_on > prev:
#             T_on = Filt_ECG[loc - 0 - Count];
#             prev = Filt_ECG[loc - 20 - Count - 1];
#             Count = Count + 1
#         P_onset.append(loc - Count)
#     return (P_location, Pamp, P_onset)
#
#
# def ecg_filters_Twave(
#         lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     h, g = sp.signal.butter(4, [20 / 500], btype='low', analog=False)
#     j, i = sp.signal.butter(2, [15 / 500], btype='low', analog=False)
#     filt_50 = sp.signal.lfilter(d, c, lead_data);  # filt_100 = sp.signal.lfilter(f, e, filt_50)
#     filt_LP = sp.signal.lfilter(h, g, filt_50)
#     smoothed = sp.signal.savgol_filter(filt_LP, window_length=19, polyorder=3)
#     n = 41;
#     baseline_wander = sp.signal.medfilt(smoothed, kernel_size=n);
#     return baseline_wander
#
#
# def T_Detection(raw_signal, R_peaks, RRint):
#     Filt_ECG = ecg_filters_V5_smooth(raw_signal)
#     T_filt = ecg_filters_Twave(raw_signal)
#     PT_twave = phasor_transform(T_filt, Rv=0.1)
#     T_st = [];
#     T_sp = [];
#     T_loc = [];
#     Tamp = []
#     for k in range(0, len(R_peaks)):
#         if k == len(R_peaks) - 1:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k - 1]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k - 1])));  # print(win_st,win_sp)
#         elif k > 0 and k < len(R_peaks) - 1:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k])));  # print(win_st,win_sp)
#         else:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k + 1]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k + 1])));  # print(win_st,win_sp)
#         # Check if the slice is empty and handle it
#         if win_st >= win_sp:
#             print(f"Warning: Empty slice for R-peak index {k}. win_st: {win_st}, win_sp: {win_sp}")
#             # Skip this iteration if the slice is empty
#             continue
#         else:
#             ch = np.max(PT_twave[win_st:win_sp])
#         loc_ch = (np.where(PT_twave == ch));
#         loc_ch = loc_ch[0];
#         # print("window :",loc_ch[0]-80,loc_ch[0]+80)
#
#         # Check if loc_ch is empty before indexing
#         if len(loc_ch) > 0:
#             ix = np.max(Filt_ECG[loc_ch[0] - 80:loc_ch[0] + 80]);
#             loc_ch = (np.where(Filt_ECG == ix));
#             loc_ch = loc_ch[0];
#             T_loc.append(loc_ch[0]);
#             Tamp.append(np.abs(Filt_ECG[loc_ch[0]]))
#             T_st.append(win_st);
#             T_sp.append(win_sp)
#         else:
#             print(f"Warning: loc_ch is empty for R-peak index {k}. Skipping this iteration.")
#             # Handle the case where loc_ch is empty, e.g., append NaN to T_loc, etc.
#
#     return (np.array(T_loc))
#
#
# def calculate_t_offset(t_loc, r_peak_ecg_signal):
#     t_offset = []
#     signal_length = len(r_peak_ecg_signal)
#
#     for o in range(len(t_loc)):
#         location = t_loc[o]
#         t_start = r_peak_ecg_signal[location]
#         t_off = r_peak_ecg_signal[min(location + 1, signal_length - 1)]
#         count = 1
#
#         while t_start < t_off and location + count + 1 < signal_length:
#             t_start = r_peak_ecg_signal[min(location + count, signal_length - 1)]
#             t_off = r_peak_ecg_signal[min(location + count + 1, signal_length - 1)]
#             count += 1
#
#         # Ensure the offset doesn't go out of bounds
#         final_offset = min(location + count + 100, signal_length - 1)
#         t_offset.append(final_offset)
#
#     return t_offset
#
#
# def calculate_snr_db(raw_signal, filtered_signal):
#     """
#     Whole-buffer SNR: compares the raw (unfiltered) signal against the
#     smoothed/filtered signal, treating the filtered signal as "signal"
#     and whatever the filter removed (raw - filtered) as "noise".
#
#     NOTE: this is an inferred standard implementation. The actual
#     calculate_snr_db() from the Colab reference notebook was not
#     provided - if it computes SNR differently, swap this out with the
#     real definition.
#     """
#     signal_power = np.mean(filtered_signal ** 2)
#     noise = raw_signal - filtered_signal
#     noise_power = np.mean(noise ** 2)
#
#     eps = 1e-12
#     if noise_power > eps:
#         return 10 * np.log10(max(signal_power, eps) / noise_power)
#     else:
#         return float("inf")
#
#
# def bandpass(signal_in, fs, lowcut=30, highcut=499.9, order=4):
#     # Matched to the validated Colab reference implementation.
#     # highcut=499.9 at fs=1000 sits right at Nyquist, so this is
#     # effectively just a 30Hz high-pass (removes baseline wander,
#     # keeps almost the full remaining spectrum) rather than the
#     # narrower 30-100Hz band this app used previously. That narrower
#     # band was attenuating/shifting the sharp A-wave and H-onset
#     # deflections the detection logic depends on.
#     nyq = 0.5 * fs
#     b, a = butter(order, [lowcut / nyq, highcut / nyq], btype='band')
#     return filtfilt(b, a, signal_in)
#
#
# # ================================================================
# #   SHARED REAL ALGORITHM
# #   This is the single source of truth for PA / AH / HV / PR /
# #   QRS / SNR. Both process() (console/debug) and analyzelead4()
# #   (called from Android) call this so they can never drift out
# #   of sync again.
# # ================================================================
#
# def compute_his_bundle_intervals(v2, fs=1000, lsb_V=286e-9):
#     """
#     v2: already-scaled, already-inverted lead signal in mV, i.e.
#         v2 = (raw_samples * 0.000286) * (-gain)
#     Returns a dict with PA, AH, HV, PR, QRS (all ms) and SNR (dB).
#     Raises ValueError if a clean best-beat/segment could not be
#     resolved from this buffer (e.g. buffer too short / too noisy).
#     """
#
#     ###--------------------------- Raw_data fetching and R peak detection ----------------------------###
#     R_data = v2 * (-1)
#     R_peak_ecg_signal = ecg_filters_V5_01(R_data)
#     R_Location, ectopic, S_Location = R_Peak_Detection_05(R_data)
#     R_Location = np.array(R_Location, dtype=int)
#
#     if len(R_Location) < 3:
#         raise ValueError("Not enough R peaks detected in this buffer.")
#
#     ###--------------------------Heart Rate calculation---------------------------------- ###
#     R_amp = [];
#     RRint = [];
#     for i in range(0, len(R_Location) - 1):
#         ch = R_Location[i + 1] - R_Location[i];
#         RRint.append(ch)
#         R_amp.append(R_peak_ecg_signal[R_Location[i]])
#         if i == (len(R_Location) - 2):
#             R_amp.append(R_peak_ecg_signal[R_Location[i + 1]])
#
#     QRS_Onset, QRS_Offset = detect_qrs_onset_offset(R_Location, R_peak_ecg_signal)
#     R_Loc_New, PR_Array = calculate_pr_interval(R_Location, QRS_Onset, R_peak_ecg_signal)
#     Mode_Value = calculate_mode(PR_Array)
#     New_QRS_Onset, New_QRS_Offset = calculate_new_qrs_onset_offset(R_Location, R_peak_ecg_signal,
#                                                                    Mode_Value)
#     P_location, Pamp, P_onset = P_Detection(R_data, R_Location, RRint, R_amp)
#
#     filtered_v2 = ecg_filters_V5_smooth(v2)
#     R_data = filtered_v2 * (-1)
#
#     # ---------- Whole-buffer SNR ----------
#     # Matched to the Colab reference: compares the ORIGINAL raw signal
#     # against the SMOOTHED signal across the entire buffer, not just
#     # the one selected beat's tiny His-bundle window. This measures
#     # overall recording quality rather than one beat's local quality.
#     raw_signal = v2 * (-1)
#     filtered_signal = R_data
#     SNR_dB = calculate_snr_db(raw_signal, filtered_signal)
#
#     R_peak_ecg_signal = ecg_filters_V5_smooth(R_data)
#     R_Location, ectopic, S_Location = R_Peak_Detection_05(R_data)
#     T_loc = T_Detection(R_peak_ecg_signal, R_Location, RRint)
#     T_Offset = calculate_t_offset(T_loc, R_peak_ecg_signal)
#
#     T_Offset = np.array(T_Offset, dtype=int)
#     P_location = np.array(P_location, dtype=int)
#     P_onset = np.array(P_onset, dtype=int)
#
#     ads_values_tloc_p_location = []
#     for i, t_offset in enumerate(T_Offset):
#         next_p_onset = next((p for p in P_onset if p > t_offset), None)
#         if next_p_onset is not None and next_p_onset < len(R_peak_ecg_signal):
#             seg_start = int(t_offset)
#             seg_end = int(next_p_onset)
#             ads_segment = np.round(np.abs(R_peak_ecg_signal[seg_start:seg_end] / 0.000286)).astype(
#                 int)
#             ads_values_tloc_p_location.append(ads_segment)
#
#     if not ads_values_tloc_p_location:
#         raise ValueError("No T→P segments found for ADS analysis.")
#
#     # -------------------------------------------------------------------------
#     #                SELECT BEST 5 SEGMENTS (by RMS voltage)
#     # -------------------------------------------------------------------------
#     segment_scores = []
#     segment_indices = []
#
#     for i, ads_segment in enumerate(ads_values_tloc_p_location):
#         ads_segment = np.asarray(ads_segment, dtype=float)
#         if len(ads_segment) < 10:  # ignore very small segments
#             continue
#         voltage = ads_segment * lsb_V
#         rms = np.sqrt(np.mean(voltage ** 2))
#         segment_scores.append(rms)
#         segment_indices.append(i)
#
#     segment_scores = np.array(segment_scores)
#     segment_indices = np.array(segment_indices)
#
#     if len(segment_indices) == 0:
#         raise ValueError("No valid segments (all shorter than 10 samples) for ADS analysis.")
#
#     # Matched to the validated Colab reference implementation: sort
#     # DESCENDING and take the highest-RMS segments first.
#     #
#     # NOTE: an earlier revision of this file inverted this to
#     # ascending/lowest-RMS, reasoning that a flat T->P baseline should
#     # indicate a cleaner beat. That reasoning turned out to be wrong -
#     # the user's Colab notebook (validated against known-correct
#     # values) uses this original descending/highest-RMS-first order,
#     # so it has been reverted to match.
#     sorted_order = np.argsort(segment_scores)[::-1]  # descending: highest RMS first
#     candidate_indices = segment_indices[sorted_order]  # ALL candidates, best (highest RMS) first
#
#     # Try every candidate beat, best (quietest baseline) first, and use
#     # the first one that actually passes validation. The old code only
#     # ever tried the single top-ranked candidate and raised/crashed the
#     # ENTIRE buffer's analysis if that one beat happened to fail a
#     # validity check (e.g. "P-onset is not before QRS-onset") - even
#     # though 4+ other perfectly usable candidate beats had already been
#     # computed and were sitting right there unused.
#     last_error = None
#     result_core = None
#
#     for candidate_idx in candidate_indices:
#         try:
#             result_core = _evaluate_candidate_beat(
#                 candidate_idx,
#                 P_onset=P_onset,
#                 R_Location=R_Location,
#                 New_QRS_Onset=New_QRS_Onset,
#                 New_QRS_Offset=New_QRS_Offset,
#                 R_peak_ecg_signal=R_peak_ecg_signal,
#                 fs=fs,
#             )
#             break
#         except ValueError as e:
#             last_error = e
#             continue
#
#     if result_core is None:
#         raise ValueError(
#             f"No usable beat found among {len(candidate_indices)} candidates "
#             f"(last error: {last_error})."
#         )
#
#     # Apply the whole-buffer SNR (matched to Colab) computed earlier,
#     # rather than a per-beat value - _evaluate_candidate_beat leaves
#     # "SNR" as None on purpose since this is the single source of truth.
#     result_core["SNR"] = float(SNR_dB)
#
#     return result_core
#
#
# def _evaluate_candidate_beat(
#         candidate_idx,
#         P_onset,
#         R_Location,
#         New_QRS_Onset,
#         New_QRS_Offset,
#         R_peak_ecg_signal,
#         fs,
# ):
#     """
#     Attempt to compute PA/AH/HV/PR/QRS/SNR for ONE candidate beat.
#     Raises ValueError if this particular beat doesn't pass validation
#     (caller is expected to try the next-best candidate in that case).
#     """
#
#     if candidate_idx >= len(P_onset) or candidate_idx >= len(R_Location):
#         raise ValueError("Candidate segment index out of range for P_onset / R_Location.")
#
#     R_peak_i = int(R_Location[candidate_idx])
#
#     # ---------- QRS onset ----------
#     qrs_candidates = [q for q in New_QRS_Onset if q < R_peak_i]
#     if len(qrs_candidates) == 0:
#         raise ValueError("No QRS onset found before this candidate's R peak.")
#     QRS_on_i = qrs_candidates[-1]
#
#     # ---------- QRS offset ----------
#     qrs_off_candidates = [q for q in New_QRS_Offset if q > R_peak_i]
#     if len(qrs_off_candidates) == 0:
#         raise ValueError("No QRS offset found after this candidate's R peak.")
#     QRS_off_i = qrs_off_candidates[0]
#
#     # ---------- P onset for this beat ----------
#     p_on_i = int(P_onset[candidate_idx])
#     if p_on_i >= QRS_on_i:
#         raise ValueError("P-onset is not before QRS-onset for this candidate beat.")
#
#     # ---------- Bandpass the P-onset -> QRS-onset segment ----------
#     seg_start = p_on_i
#     seg_end = QRS_on_i + 1
#     raw_segment = R_peak_ecg_signal[seg_start:seg_end]
#
#     if len(raw_segment) <= 10:
#         raise ValueError("P-onset to QRS-onset segment too short to bandpass filter.")
#
#     bp = bandpass(raw_segment, fs)
#
#     # ---------- Scaled bandpassed trace for graphing ----------
#     # Matches Colab's bp_scaled: normalize the bandpassed (His-Purkinje)
#     # component to +-1 and rescale it to roughly the same visual
#     # amplitude as the raw segment, purely so it can be overlaid on the
#     # raw trace and stay visible instead of looking flat next to the
#     # much larger QRS/P-wave amplitudes.
#     bp_abs_max = np.max(np.abs(bp))
#     if bp_abs_max > 0:
#         bp_norm = bp / bp_abs_max
#         scale = 0.5 * np.max(np.abs(raw_segment))
#         bp_scaled = bp_norm * scale
#     else:
#         bp_scaled = bp
#
#     # ---------- A wave (true peak in early PR region) ----------
#     qrs_local = QRS_on_i - seg_start
#     search_end = int(0.45 * qrs_local)
#     sub = bp[15:search_end]
#     if len(sub) == 0:
#         raise ValueError("A-wave search window is empty for this candidate beat.")
#     peak_idx = np.argmax(sub)
#     A_wave_i = seg_start + 15 + peak_idx
#
#     # -------------------------------------------------------
#     #        H_on and H_off DETECTION (TRUE BIPHASIC START)
#     # -------------------------------------------------------
#     H_on_i = None
#     bp_segment = bp  # bandpassed signal
#
#     # search region before QRS (where the circled wave is)
#     search_start = max(0, qrs_local - 60)
#     search_end = qrs_local - 5
#     sub = bp_segment[search_start:search_end]
#
#     # Step 1: find zero crossing (center of biphasic)
#     zc_idx = None
#     for i in range(1, len(sub)):
#         if sub[i - 1] < 0 and sub[i] > 0:
#             zc_idx = i
#             break
#
#     # Step 2: go BACK to find beginning of that wave
#     if zc_idx is not None:
#         for i in range(zc_idx, 1, -1):
#             # beginning = where slope starts increasing
#             if abs(sub[i] - sub[i - 1]) < 0.001:
#                 H_on_i = seg_start + search_start + i
#                 break
#
#     # search between H_on and QRS
#     H_off_i = None
#     if H_on_i is not None:
#         h_on_local = H_on_i - seg_start
#
#         search_start = h_on_local + 5
#         search_end = qrs_local - 2
#
#         sub = bp_segment[search_start:search_end]
#
#         if len(sub) > 5:
#             # find LAST zero-crossing before QRS
#             for i in range(len(sub) - 1, 1, -1):
#                 if sub[i - 1] > 0 and sub[i] < 0:
#                     H_off_i = seg_start + search_start + i
#                     break
#
#     if H_on_i is None:
#         # Fall back rather than fail this candidate outright.
#         H_on_i = A_wave_i
#
#     if H_off_i is None:
#         # No biphasic-end crossing found (or H_on itself wasn't found) -
#         # fall back to QRS onset, same boundary the original HV interval
#         # calculation below already uses.
#         H_off_i = QRS_on_i
#
#     # ---------- Intervals ----------
#     PA_ms = A_wave_i - p_on_i
#     AH_ms = H_on_i - A_wave_i
#     HV_ms = QRS_on_i - H_on_i
#     PR_ms = QRS_on_i - p_on_i
#     QRS_ms = QRS_off_i - QRS_on_i
#
#     return {
#         "PA": float(PA_ms),
#         "AH": float(AH_ms),
#         "HV": float(HV_ms),
#         "PR": float(PR_ms),
#         "QRS": float(QRS_ms),
#         # SNR is intentionally NOT set here - compute_his_bundle_intervals
#         # overrides it with the whole-buffer SNR (matching Colab), since
#         # per-beat SNR was superseded by that whole-buffer measurement.
#         "SNR": None,
#         # Landmark sample indices for the beat that was actually used,
#         # so a caller can plot exactly what was measured instead of
#         # just the raw trace. Not part of the original 6-value result,
#         # purely additive - existing callers that only read
#         # PA/AH/HV/PR/QRS/SNR are unaffected.
#         "R_peak_i": R_peak_i,
#         "p_on_i": p_on_i,
#         "A_wave_i": A_wave_i,
#         "H_on_i": H_on_i,
#         "H_off_i": H_off_i,
#         "QRS_on_i": QRS_on_i,
#         "QRS_off_i": QRS_off_i,
#         "seg_start": seg_start,
#         "seg_end": seg_end,
#         "bp_scaled": bp_scaled,
#     }
#
#
# def analyzelead4(samples, graph_path):
#     """
#     Analyze Lead4 ECG data and return ECG intervals.
#     Returns: [PA, AH, HV, PR, QRS, SNR, graph_path]
#     (HH has been removed — it is no longer computed or returned.)
#     This function is called from Android OfflineProcessor.
#     """
#     try:
#         import matplotlib
#         matplotlib.use("Agg")
#         import matplotlib.pyplot as plt
#
#         fs = 1000
#
#         # ----------------------------------
#         # Convert Java ArrayList to Python list
#         # ----------------------------------
#         try:
#             # Direct conversion from Java ArrayList to NumPy (faster)
#             size = samples.size()
#             lead4 = np.zeros(size, dtype=np.float64)
#             for i in range(size):
#                 lead4[i] = float(samples.get(i))
#         except Exception:
#             # Already Python list
#             lead4 = np.array([float(x) for x in samples], dtype=np.float64)
#
#         # Quick validation
#         if len(lead4) == 0:
#             return [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ""]
#
#         # ----------------------------------
#         # Convert Lead4 to V2 (mV)
#         # ----------------------------------
#         gain = 1
#         scale = 0.000286
#         v2 = ((lead4) * scale) * (-(gain))
#
#         # ----------------------------------
#         # Real interval detection (same algorithm process() uses)
#         # ----------------------------------
#         result = compute_his_bundle_intervals(v2, fs=fs)
#
#         # ----------------------------------
#         # Generate Graph
#         # ----------------------------------
#         # Two panels: (1) the full raw trace for context, and (2) a
#         # zoomed-in view of the actual beat that was measured, with the
#         # detected landmarks marked. The old graph only showed panel 1,
#         # which meant there was no way to visually confirm WHICH beat
#         # was used or WHERE the PA/AH/HV/PR/QRS boundaries were placed.
#         fig, axes = plt.subplots(2, 1, figsize=(12, 9))
#
#         axes[0].plot(v2, color='blue', linewidth=0.8)
#         axes[0].set_title('Full ECG Trace (Lead4 / V2)')
#         axes[0].set_xlabel('Sample')
#         axes[0].set_ylabel('Amplitude (mV)')
#         axes[0].grid(True)
#
#         margin = 100
#         zoom_start = max(0, result["seg_start"] - margin)
#         zoom_end = min(len(v2), result["QRS_off_i"] + margin)
#
#         if zoom_end > zoom_start:
#             x_zoom = np.arange(zoom_start, zoom_end)
#             axes[1].plot(x_zoom, v2[zoom_start:zoom_end], color='black', linewidth=1.0,
#                          label='Raw beat')
#
#             # Overlay the bandpassed His-Purkinje component (Colab's
#             # bp_scaled), aligned to its own P-onset -> QRS-onset x-range
#             # rather than the wider zoom window, and scaled for
#             # visibility against the much larger QRS/P amplitudes.
#             bp_scaled = result.get("bp_scaled")
#             if bp_scaled is not None and len(bp_scaled) > 0:
#                 x_bp = np.arange(result["seg_start"], result["seg_start"] + len(bp_scaled))
#                 axes[1].plot(x_bp, bp_scaled, color='teal', linewidth=1.2, linestyle='-',
#                              alpha=0.85, label='Bandpassed His signal (scaled)')
#
#             landmarks = [
#                 ('P-onset', result["p_on_i"], 'green'),
#                 ('A-wave', result["A_wave_i"], 'orange'),
#                 ('H-onset', result["H_on_i"], 'purple'),
#                 ('H-offset', result["H_off_i"], 'brown'),
#                 ('QRS-onset', result["QRS_on_i"], 'red'),
#                 ('QRS-offset', result["QRS_off_i"], 'red'),
#                 ('R-peak', result["R_peak_i"], 'blue'),
#             ]
#
#             y_top = np.max(v2[zoom_start:zoom_end])
#             for label, idx, color in landmarks:
#                 if zoom_start <= idx < zoom_end:
#                     axes[1].axvline(idx, color=color, linestyle='--', linewidth=1)
#                     axes[1].text(idx, y_top, label, rotation=90,
#                                  va='top', ha='right', fontsize=8, color=color)
#
#             axes[1].legend(loc='lower right', fontsize=8)
#
#         axes[1].set_title('Best His Bundle Segment (Detected Landmarks)')
#         axes[1].set_xlabel('Sample')
#         axes[1].set_ylabel('Amplitude (mV)')
#         axes[1].grid(True)
#
#         plt.tight_layout()
#         plt.savefig(graph_path, dpi=150, bbox_inches='tight')
#         plt.close()
#
#         # ----------------------------------
#         # Return: [PA, AH, HV, PR, QRS, SNR, graph_path]
#         # ----------------------------------
#         return [
#             result["PA"],
#             result["AH"],
#             result["HV"],
#             result["PR"],
#             result["QRS"],
#             result["SNR"],
#             str(graph_path)
#         ]
#
#     except Exception as e:
#         import traceback
#         print("================================")
#         print("PYTHON ERROR in analyzelead4")
#         print(str(e))
#         traceback.print_exc()
#         print("================================")
#         return [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ""]
#
#
# def process(buffer2):
#     """
#     Console/debug entry point. Uses the exact same real algorithm
#     as analyzelead4() via compute_his_bundle_intervals(), then
#     prints a formatted table.
#     """
#     fs = 1000
#
#     v2 = np.array(buffer2, dtype=np.float64)
#     gain = 1
#     scale = 0.000286
#     # Convert to mV and invert
#     v2 = ((v2) * scale) * (-(gain));
#
#     result = compute_his_bundle_intervals(v2, fs=fs)
#
#     PA_ms = result["PA"]
#     AH_ms = result["AH"]
#     HV_ms = result["HV"]
#     PR_ms = result["PR"]
#     QRS_ms = result["QRS"]
#     SNR_dB = result["SNR"]
#
#     ### Display #####
#     df = pd.DataFrame(
#         [[int(PA_ms), int(AH_ms), int(HV_ms), int(PR_ms), int(QRS_ms)]],
#         columns=[
#             "PA (ms)",
#             "AH (ms)",
#             "HV (ms)",
#             "PR (ms)",
#             "QRS (ms)"
#         ]
#     )
#
#     # ANSI escape codes
#     BOLD = "\033[1m"
#     BLACK = "\033[30m"
#     RESET = "\033[0m"
#     RED = "\033[91m"
#     BLUE = "\033[94m"
#     print("\n")
#     print(BOLD + BLACK + "=" * 60 + RESET)
#     print(BOLD + BLACK + "           BEST  HIS BUNDLE SEGMENT" + RESET)
#     print(BOLD + BLACK + "=" * 60 + RESET)
#     print()
#     print(BOLD + df.to_string(index=False) + RESET)
#     print(BOLD + BLACK + "=" * 60 + RESET)
#     print()
#
#     snr_color = RED if SNR_dB < 6 else BLUE
#     print(f"{BOLD}{snr_color}SNR of Best Segment : {SNR_dB:.2f} dB{RESET}")
#     print(BOLD + BLACK + "=" * 60 + RESET)
#
#     return {
#         "PA": PA_ms,
#         "AH": AH_ms,
#         "HV": HV_ms,
#         "PR": PR_ms,
#         "QRS": QRS_ms,
#         "SNR": SNR_dB
#     }


#  POULAMI PYTHON CODE
# def analyze_his_bundle(v2):
#     PA_ms = int(PA_ms)
#     AH_ms = int(AH_ms)
#     HV_ms = int(HV_ms)
#     PR_ms = int(PR_ms)
#     QRS_ms = int(QRS_ms)
#
#     return PA_ms, AH_ms, HV_ms, PR_ms, QRS_ms
#
# ## data loss correction ##
# def data_loss(input_signal):
#     for x in range(1, len(input_signal) - 1, 1):  #### data loss
#         if (input_signal[
#             x] == 0):  ## checks zero values in input signal and replaces with the mean of adjacent non zero values
#             input_signal[x] = st.mean([input_signal[x - 1], input_signal[x + 1]])
#     return (input_signal)
#
#
# ## time stamp ##
# def get_time_from_timestamp(
#         timestamp):  ## converting unit timestamp to a readable date and time format
#     read_able = datetime.datetime.fromtimestamp(timestamp)
#     now_asia = str(read_able.astimezone(timezone('Asia/Kolkata')));  # print(now_asia)
#     year = now_asia[0:10]
#     year = year[8:10] + "-" + year[5:7] + "-" + year[0:4]
#     time = now_asia[11:19]
#     return (year, time)
#
#
# ## function extracts values from a dictionary given a specific index ##
# def Value_axis(data_dict, value):
#     data = list(data_dict.items())
#     an_array = np.array(data, dtype=object)
#     Values_1 = an_array[value]
#     Values_2 = np.asarray(Values_1[1])
#     return Values_2
#
#
# def ecg_filters_V5_smooth(lead_data, hp=0.67):
#     """
#     ECG filter according to IEC 60601-2-25
#     - Baseline wander removal
#     - Low-pass decomposition
#     - Smooth reconstruction
#     """
#
#     lead_data = np.asarray(lead_data, dtype=float).copy()
#
#     # Replace zero samples
#     for i in range(1, len(lead_data)):
#         if lead_data[i] == 0:
#             lead_data[i] = lead_data[i - 1]
#
#     # ------------------------------------------------------------------
#     # Baseline Wander Removal
#     # ------------------------------------------------------------------
#     b = signal.firwin(
#         2377,
#         cutoff=hp / 500,
#         window="hamming",
#         pass_zero=False
#     )
#
#     filt_BW = signal.filtfilt(b, [1], lead_data)
#
#     # ------------------------------------------------------------------
#     # Low-pass filters
#     # ------------------------------------------------------------------
#     lp45_b, lp45_a = signal.butter(
#         2,
#         45 / 500,
#         btype='low'
#     )
#
#     lp15_b, lp15_a = signal.butter(
#         2,
#         15 / 500,
#         btype='low'
#     )
#
#     filt_SM = signal.lfilter(lp15_b, lp15_a, filt_BW)
#     filt_LP = signal.lfilter(lp45_b, lp45_a, filt_BW)
#
#     # ------------------------------------------------------------------
#     # High-frequency component
#     # ------------------------------------------------------------------
#     sparse = filt_LP - filt_SM
#
#     # ------------------------------------------------------------------
#     # Replace TV denoising with Savitzky-Golay smoothing
#     # ------------------------------------------------------------------
#     denoise = signal.savgol_filter(
#         sparse,
#         window_length=29,
#         polyorder=3
#     )
#
#     # ------------------------------------------------------------------
#     # Reconstruct signal
#     # ------------------------------------------------------------------
#     smooth_data = filt_SM + denoise
#
#     return smooth_data
#
#
# def baseline_filter(lead_data, hp=0.67):
#     ##### Baseline filter without ST Segment distortion
#     b = signal.firwin(2377, cutoff=[hp / 500], window="hamming", pass_zero=False);
#     a = 1
#     filt_BW = signal.filtfilt(b, a, lead_data);
#     return filt_BW
#
#     PT[i] = math.degrees(math.atan(ch))
#     # PT[i] = Rv+ ()
#
#
# return (PT)
#
#
# def find_PVC(signal, Rpeaks, Ramplitued, RRinterval):
#     ##   LIST OF AREA UNDER THE CURVE
#     AUC = collections.deque()
#     for k in Rpeaks:
#         ch = abs(np.trapezoid(signal[k - 80:k + 80]));
#         AUC.append(ch)
#         # print("median : ",ch, 1.3*st.median(AUC))
#     AUC = np.array(AUC);  # print(len(AUC),AUC)
#     ##    ECTOPIC FUNCTION
#     PVC_list = np.empty_like(Rpeaks, dtype=float);
#     PVC_list[:] = np.nan
#     SVC_list = np.empty_like(Rpeaks, dtype=float);
#     SVC_list[:] = np.nan
#     for i in range(1, len(RRinterval)):
#         if RRinterval[i] >= (1.5 * RRinterval[i - 1]):
#             if Ramplitued[i] >= (Ramplitued[i - 1] + (0.2 * Ramplitued[i - 1])):  # PVC
#                 if AUC[i] > (1.3 * st.median(AUC[0:i - 1])):
#                     PVC_list[i] = Rpeaks[i]
#             else:  # SVC or PVC
#                 if i > 1:
#                     if AUC[i] > (1.3 * st.median(AUC[0:i - 1])):
#                         PVC_list[i] = Rpeaks[i]
#     return np.array(PVC_list)
#
#
# def ecg_filters_Pwave(
#         lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     h, g = sp.signal.butter(4, [40 / 500], btype='low', analog=False);
#     j, i = signal.butter(2, [15 / 500], btype='low', analog=False)
#     filt_50 = sp.signal.lfilter(d, c, lead_data);
#     filt_100 = sp.signal.lfilter(f, e, filt_50)
#     filt_LP = sp.signal.lfilter(h, g, filt_50)
#     smoothed = sp.signal.savgol_filter(filt_LP, window_length=19, polyorder=3)
#     n = 41;
#     baseline_wander = sp.signal.medfilt(smoothed, kernel_size=n);
#     return baseline_wander
#
#
# def calculate_p_offset_p_duration_and_average_pr(p_loc, r_peak_ecg_signal, qrs_onset,
#                                                  window_size=60):
#     p_offset = []
#     p_duration = []
#     pr_differences = []
#
#     for n in range(len(p_loc)):
#         loc2 = p_loc[n] + window_size
#         p_start = r_peak_ecg_signal[loc2]
#         p_off = r_peak_ecg_signal[loc2 + 1]
#         count = 1
#
#         while p_start < p_off:
#             p_start = r_peak_ecg_signal[loc2 + count]
#             p_off = r_peak_ecg_signal[loc2 + 1 + count]
#             count = count + 1
#
#         p_offset.append(loc2 + count)
#         p_duration.append(qrs_onset[n] - (p_loc[n] - count))
#         pr_differences.append(qrs_onset[n] - (loc2 + count))
#
#     average_p_duration = np.mean(np.subtract(p_offset, p_loc))
#     average_pr = np.mean(pr_differences) + 80
#     if average_pr < 80:
#         average_pr = average_pr + 100
#     if average_pr < 0:
#         average_pr = average_pr + 150
#
#     return p_offset, p_duration, average_pr
#
#
# # def detect_qrs_onset_offset(r_location, r_peak_ecg_signal, onset_window=50,
# #                             offset_window=50):  ### onset offset change into 80 80 when bundle brunch block
# #     qrs_onset = []
# #     qrs_offset = []
# #
# #     for m in range(len(r_location)):
# #         # QRS Onset
# #         loc = r_location[m] - onset_window
# #         R_peak = r_peak_ecg_signal[loc]
# #         R_prev = r_peak_ecg_signal[loc - 1]
# #         count = 1
# #
# #         while R_peak > R_prev:
# #             R_peak = r_peak_ecg_signal[loc - count]
# #             R_prev = r_peak_ecg_signal[loc - count - 1]
# #             count = count + 1
# #
# #         qrs_onset.append(loc - count)
# #
# #         # QRS Offset
# #         loc2 = r_location[m] + offset_window
# #         R_on = r_peak_ecg_signal[loc2]
# #         R_end = r_peak_ecg_signal[loc2 + 1]
# #         count = 1
# #
# #         while R_on < R_end:
# #             R_on = r_peak_ecg_signal[loc2 + count]
# #             R_end = r_peak_ecg_signal[loc2 + 1 + count]
# #             count = count + 1
# #
# #         qrs_offset.append(loc2 + count)
# #         new_qrs_offset.append(
# #             signal_len - 1)  # If loc2 is valid but loc2+1 is not, offset is the last index
# #     else:
# #         r_on = r_peak_ecg_signal[loc2]
# #         r_end = r_peak_ecg_signal[loc2 + 1]
# #         count = 1
# #         while r_on < r_end < mode_value and (loc2 + count + 1) < signal_len:  # Added boundary check
# #             r_on = r_peak_ecg_signal[loc2 + count]
# #             r_end = r_peak_ecg_signal[loc2 + 1 + count]
# #             count = count + 1
# #         new_qrs_offset.append(min(signal_len - 1, loc2 + count - 1))  # Adjusted for consistency
# #
# #
# #              return new_qrs_onset, new_qrs_offset
#
# def detect_qrs_onset_offset(r_location, r_peak_ecg_signal,
#                             onset_window=50,
#                             offset_window=50):
#     qrs_onset = []
#     qrs_offset = []
#
#     for m in range(len(r_location)):
#
#         # QRS Onset
#         loc = r_location[m] - onset_window
#
#         R_peak = r_peak_ecg_signal[loc]
#         R_prev = r_peak_ecg_signal[loc - 1]
#
#         count = 1
#
#         while R_peak > R_prev:
#             R_peak = r_peak_ecg_signal[loc - count]
#             R_prev = r_peak_ecg_signal[loc - count - 1]
#             count += 1
#
#         qrs_onset.append(loc - count)
#
#         # QRS Offset
#         loc2 = r_location[m] + offset_window
#
#         R_on = r_peak_ecg_signal[loc2]
#         R_end = r_peak_ecg_signal[loc2 + 1]
#
#         count = 1
#
#         while R_on < R_end:
#             R_on = r_peak_ecg_signal[loc2 + count]
#             R_end = r_peak_ecg_signal[loc2 + count + 1]
#             count += 1
#
#         qrs_offset.append(loc2 + count)
#
#     return qrs_onset, qrs_offset
#
#
# def R_Peak_Detection_05(raw_data):
#     peaks_up = [];
#     amp = [];
#     peaks_up_s = [];
#     ecp = [];
#     b = signal.firwin(1377, cutoff=[2 / 500], window="hamming", pass_zero=False);
#     a = 1
#     h, g = signal.butter(4, [45 / 500], btype='low', analog=False)
#     filt_BW = signal.filtfilt(b, a, raw_data);
#     data_F = signal.lfilter(h, g, filt_BW)
#     data_D = np.diff(data_F);
#     data_S = data_D * data_D
#     peaks, rpeak = signal.find_peaks(data_S, distance=255, height=(max(data_S[1000:5000]) / 1.7));
#     n = len(peaks);
#
#     for i in range(0, n):
#         win = 70;
#         if (peaks[i] - (win)) < 0:
#             start_win = 0
#         else:
#             start_win = (peaks[i] - (win))
#         seg = data_F[start_win:(peaks[i]) + (
#             win)];  # seg = seg*seg; ############## Multiple Seg for getting the R or S peak
#         max_peak, peak_amp = signal.find_peaks(seg, distance=10, height=(max(seg) / 2))
#         if len(max_peak) < 2:
#             peaks_u = start_win + max_peak[0]
#         else:
#             peaks_u = start_win + np.argmax(seg)
#         peaks_up.append(peaks_u);
#
#         seg_s = seg * (-1);
#         max_peak, peak_amp = signal.find_peaks(seg_s, distance=10)  # , height = (max(seg_s)/2))
#         if len(max_peak) < 2:
#             peaks_u = start_win + max_peak[0]  ########### need fix for short S wave
#         else:
#             peaks_u = start_win + np.argmax(seg_s)
#         peaks_up_s.append(peaks_u);
#
#     rr_int = np.diff(peaks_up);
#     th_ep = st.mean(rr_int) + st.mean(rr_int) / 10
#
#     for j in range(0, len(rr_int)):
#         if rr_int[j] > th_ep:
#             ep_seg = (data_F[peaks_up[j] + 60:peaks_up[j + 1] - 60])
#             max_e = np.argmax(ep_seg * ep_seg)  ########### for irregularity needs to be fixed
#             max_e = max_e + peaks_up[j] + 60;
#             # if ep_data[max_e] > (data_F[peaks_up[j]]/2):
#             ecp.append(max_e)
#     r_peak_values = [data_F[peak] for peak in peaks_up]
#     return peaks_up, ecp, peaks_up_s
#
#
# def P_Detection(rawECG, R_peaks, RRint, Ramp):
#     Filt_ECG = ecg_filters_V5_smooth(rawECG)
#     peaks = R_peaks;
#
#     PVC = find_PVC(Filt_ECG, peaks, Ramp, RRint)
#     PT_pwave = phasor_transform(ecg_filters_Pwave(rawECG), Rv=0.05)
#     P_st = [];
#     P_sp = [];
#     loc = [];
#     Pamp = []
#     P_location = [];
#     Pst = [];
#     Psp = []
#     for k in range(0, len(peaks)):
#         if k == 0:  # 1st P-peak detection
#             win_1 = len(Filt_ECG[0:peaks[0]]);  # print(win_1)
#             if win_1 > 300:
#                 win_st = peaks[0] - 300;
#                 win_sp = peaks[0] - 80
#                 ch = np.max(PT_pwave[peaks[0] - 300:peaks[0] - 80]);
#             elif win_1 <= 300 and win_1 >= 80:
#                 win_st = 0;
#                 win_sp = peaks[0] - 80
#                 ch = np.max(PT_pwave[0:peaks[0] - 80]);
#             elif win_1 < 80:
#                 P_location.append(np.nan);
#                 win_st = np.nan;
#                 win_sp = np.nan
#                 break;
#         elif k > 0 and k < len(peaks) - 1:  # Second to Last but 1 peak
#             win_st = int(peaks[k] - (0.4 * RRint[k]));
#             win_sp = int(peaks[k] - (0.05 * (RRint[k] - 100)));  # print(win_st,win_sp)
#             ch = np.max(PT_pwave[win_st:win_sp]);
#         else:  # Last peak
#             win_st = int(peaks[k] - (0.4 * RRint[k - 1]));
#             win_sp = int(peaks[k] - (0.05 * (RRint[k - 1] - 100)));  # print(win_st,win_sp)
#             ch = np.max(PT_pwave[win_st:win_sp]);
#         loc_ch = (np.where(PT_pwave == ch));
#         loc_ch = loc_ch[0];
#         if loc_ch[0] < 80:
#             loc_st = loc_ch[0]
#         else:
#             loc_st = loc_ch[0] - 80
#         ix = np.max(Filt_ECG[loc_st:loc_ch[0] + 80]);
#         loc_ch = (np.where(Filt_ECG == ix));
#         loc_ch = loc_ch[0];
#         loc.append(loc_ch[0]);
#         Pamp.append(np.abs(Filt_ECG[loc_ch[0]]))
#         P_st.append(win_st);
#         P_sp.append(win_sp)
#
#     # print("Pamp temp :", len(Pamp), Pamp);  print("Ploc temp :", len(loc), loc)
#
#     for m in range(0, len(RRint)):
#         # print(m)
#         if np.isnan(PVC[m]) == True and Pamp[m] > (
#                 0.5 * Filt_ECG[peaks[m]]):  # Beat Not a PVC: retain P loc
#             P_location.append(loc[m])
#             Pst.append(P_st[m]);
#             Psp.append(P_sp[m])
#             '''if m>1 and (RRint[m]>(1.6*RRint[m-1])):    #Beat not 1st and RR > 1.6*prevRR :: check dissosiated P wave
#               win_st = int(peaks[m]+400); win_sp = int(loc[m+1]-400);
#               print(m, win_st, win_sp)
#               ch = np.max(PT_pwave[win_sp:win_st]); loc_ch = (np.where(PT_pwave==ch)); loc_ch = loc_ch[0];
#               ix = np.max(Filt_ECG[loc_ch[0]-80:loc_ch[0]+80]); loc_ch = (np.where(Filt_ECG==ix)); loc_ch = loc_ch[0];
#               amp = np.abs(Filt_ECG[loc_ch[0]])
#               if amp>(0.05*Filt_ECG[peaks[m]]):    #Pamp>0.05*QRS amp: Dissosiated P detected
#                 P_location.append(loc_ch[0]); Pamp = np.insert(Pamp, m, (Filt_ECG[loc_ch[0]]))
#                 Pst.append(win_st); Psp.append(win_sp)'''
#
#     P_location = loc
#     P_onset = [];  # print("P location : ",P_location)
#     # print("Pst :", Pst); print("Psp :",Psp)
#     for m in range(len(P_location)):
#         loc = P_location[m]
#         T_on = Filt_ECG[loc - 0];
#         prev = Filt_ECG[loc - 20 - 1];
#         Count = 1
#         while T_on > prev:
#             T_on = Filt_ECG[loc - 0 - Count];
#             prev = Filt_ECG[loc - 20 - Count - 1];
#             Count = Count + 1
#         P_onset.append(loc - Count);
#     return (P_location, Pamp, P_onset)
#
#
# def ecg_filters_Twave(
#         lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     h, g = sp.signal.butter(4, [20 / 500], btype='low', analog=False)
#     j, i = sp.signal.butter(2, [15 / 500], btype='low', analog=False)
#     filt_50 = sp.signal.lfilter(d, c, lead_data);  # filt_100 = sp.signal.lfilter(f, e, filt_50)
#     filt_LP = sp.signal.lfilter(h, g, filt_50)
#     smoothed = sp.signal.savgol_filter(filt_LP, window_length=19, polyorder=3)
#     n = 41;
#     baseline_wander = sp.signal.medfilt(smoothed, kernel_size=n);
#     return baseline_wander
#
#
# def T_Detection(raw_signal, R_peaks, RRint):
#     Filt_ECG = ecg_filters_V5_smooth(raw_signal)
#     T_filt = ecg_filters_Twave(raw_signal)
#     PT_twave = phasor_transform(T_filt, Rv=0.1)
#     T_st = [];
#     T_sp = [];
#     T_loc = [];
#     Tamp = []
#     for k in range(0, len(R_peaks)):
#         if k == len(R_peaks) - 1:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k - 1]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k - 1])));  # print(win_st,win_sp)
#         elif k > 0 and k < len(R_peaks) - 1:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k])));  # print(win_st,win_sp)
#         else:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k + 1]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k + 1])));  # print(win_st,win_sp)
#         # Check if the slice is empty and handle it
#         if win_st >= win_sp:
#             print(f"Warning: Empty slice for R-peak index {k}. win_st: {win_st}, win_sp: {win_sp}")
#             # Skip this iteration if the slice is empty
#             continue
#         else:
#             ch = np.max(PT_twave[win_st:win_sp])
#         loc_ch = (np.where(PT_twave == ch));
#         loc_ch = loc_ch[0];
#         # print("window :",loc_ch[0]-80,loc_ch[0]+80)
#
#         # Check if loc_ch is empty before indexing
#         if len(loc_ch) > 0:
#             ix = np.max(Filt_ECG[loc_ch[0] - 80:loc_ch[0] + 80]);
#             loc_ch = (np.where(Filt_ECG == ix));
#             loc_ch = loc_ch[0];
#             T_loc.append(loc_ch[0]);
#             Tamp.append(np.abs(Filt_ECG[loc_ch[0]]))
#             T_st.append(win_st);
#             T_sp.append(win_sp)
#         else:
#             print(f"Warning: loc_ch is empty for R-peak index {k}. Skipping this iteration.")
#             # Handle the case where loc_ch is empty, e.g., append NaN to T_loc, etc.
#
#     return (np.array(T_loc))
#
#
# def calculate_t_offset(t_loc, r_peak_ecg_signal):
#     t_offset = []
#     signal_length = len(r_peak_ecg_signal)
#
#     for o in range(len(t_loc)):
#         location = t_loc[o]
#         t_start = r_peak_ecg_signal[location]
#         t_off = r_peak_ecg_signal[min(location + 1, signal_length - 1)]
#         count = 1
#
#         while t_start < t_off and location + count + 1 < signal_length:
#             t_start = r_peak_ecg_signal[min(location + count, signal_length - 1)]
#             t_off = r_peak_ecg_signal[min(location + count + 1, signal_length - 1)]
#             count += 1
#
#         # Ensure the offset doesn't go out of bounds
#         final_offset = min(location + count + 100, signal_length - 1)
#         t_offset.append(final_offset)
#
#     return t_offset
#
#
# # ==========================================================
# #               SNR CALCULATION
# # ==========================================================
#
# def calculate_snr_db(raw_signal, filtered_signal):
#     raw_signal = np.asarray(raw_signal, dtype=float)
#     filtered_signal = np.asarray(filtered_signal, dtype=float)
#
#     noise = raw_signal - filtered_signal
#
#     signal_power = np.mean(filtered_signal ** 2)
#     noise_power = np.mean(noise ** 2)
#
#     if noise_power <= 1e-20:
#         return 99.0
#
#     larger = max(signal_power, noise_power)
#     smaller = max(min(signal_power, noise_power), 1e-20)
#
#     return 10 * np.log10(larger / smaller)
#
#
# fs = 1000
# lsb_V = 286e-9
# ###--------------------------- Raw_data fetching and R peak detection ----------------------------###
# R_data = v2 * (-1)
# R_peak_ecg_signal = ecg_filters_V5_01(R_data)
# R_Location, ectopic, S_Location = R_Peak_Detection_05(R_data)
# R_Location = np.array(R_Location, dtype=int)
#
# ###--------------------------Heart Rate calculation---------------------------------- ###
# R_amp = [];
# RRint = [];
# for i in range(0, len(R_Location) - 1):
#     ch = R_Location[i + 1] - R_Location[i];
#     RRint.append(ch)
#     R_amp.append(R_peak_ecg_signal[R_Location[i]])
#     if i == (len(R_Location) - 2):
#         R_amp.append(R_peak_ecg_signal[R_Location[i + 1]])
#
# QRS_Onset, QRS_Offset = detect_qrs_onset_offset(R_Location, R_peak_ecg_signal)
# R_Loc_New, PR_Array = calculate_pr_interval(R_Location, QRS_Onset, R_peak_ecg_signal)
# distance_between_QRS_on_R_peak = np.subtract(R_Location, QRS_Onset)
# # avg_distance_between_QRS_on_R_peak=np.mean(distance_between_QRS_on_R_peak)
# Mode_Value = calculate_mode(PR_Array)
# New_QRS_Onset, New_QRS_Offset = calculate_new_qrs_onset_offset(R_Location, R_peak_ecg_signal,
#                                                                Mode_Value)
# new_QRS_difference = np.subtract(New_QRS_Offset, New_QRS_Onset)
# new_average_QRS = np.mean(new_QRS_difference)
# PVC = find_PVC(R_data, R_Location, R_amp, RRint)  #### not added
# P_location, Pamp, P_onset = P_Detection(R_data, R_Location, RRint, R_amp)
# P_Offset, P_Duration, Average_PR = calculate_p_offset_p_duration_and_average_pr(P_location, R_data,
#                                                                                 New_QRS_Onset)  #### not added
#
# filtered_v2 = ecg_filters_V5_smooth(v2)
#
# R_data = filtered_v2 * (-1)
# # -------------------------------------------------------
# #           SNR OF CURRENT TIMESTAMP
# # -------------------------------------------------------
#
# raw_signal = v2 * (-1)
# filtered_signal = R_data
# SNR_dB = calculate_snr_db(raw_signal, filtered_signal)
# R_peak_ecg_signal = ecg_filters_V5_smooth(R_data)
# R_Location, ectopic, S_Location = R_Peak_Detection_05(R_data)
# T_loc = T_Detection(R_peak_ecg_signal, R_Location, RRint)
# T_Offset = calculate_t_offset(T_loc, R_peak_ecg_signal)
# valid_indices = [int(idx) for idx in T_Offset if idx < len(R_peak_ecg_signal)]  #### not added
# # Convert to arrays
# T_Offset = np.array(T_Offset, dtype=int)
# P_location = np.array(P_location, dtype=int)
# P_onset = np.array(P_onset, dtype=int)
# ads_values_tloc_p_location = []
# for i, t_offset in enumerate(T_Offset):
#     # Find the next valid P_onset that occurs AFTER this T_offset
#     next_p_onset = next((p for p in P_onset if p > t_offset), None)
#
#     if next_p_onset is not None and next_p_onset < len(R_peak_ecg_signal):
#         seg_start = int(t_offset)
#         seg_end = int(next_p_onset)
#
#         # Extract ADS segment
#         ads_segment = np.round(np.abs(R_peak_ecg_signal[seg_start:seg_end] / 0.000286)).astype(int)
#         ads_values_tloc_p_location.append(ads_segment)
#
# # if not ads_values_tloc_p_location:
# #     raise ValueError("No T\u2192P segments found for ADS analysis.")
#
# segment_max_V = []
# segment_max_mV = []
#
# for i, ads_segment in enumerate(ads_values_tloc_p_location):
#     ads_segment = np.asarray(ads_segment, dtype=float)
#     voltage_V = ads_segment * lsb_V
#     max_v_V = np.max(voltage_V)
#     max_v_mV = max_v_V * 1e3
#     segment_max_V.append(max_v_V)
#     segment_max_mV.append(max_v_mV)
#
# segment_max_V = np.array(segment_max_V)
# segment_max_mV = np.array(segment_max_mV)
# # -------------------------------------------------------------------------
# #                SELECT BEST 5 SEGMENTS
# # -------------------------------------------------------------------------
#
# segment_scores = []
# segment_indices = []
#
# for i, ads_segment in enumerate(ads_values_tloc_p_location):
#     ads_segment = np.asarray(ads_segment, dtype=float)
#
#     if len(ads_segment) < 10:  # ignore very small segments
#         continue
#
#     # Convert to voltage
#     voltage = ads_segment * lsb_V
#
#     # Score = RMS (robust metric)
#     rms = np.sqrt(np.mean(voltage ** 2))
#
#     segment_scores.append(rms)
#     segment_indices.append(i)
#
# # Convert to numpy
# segment_scores = np.array(segment_scores)
# segment_indices = np.array(segment_indices)
#
# # Get top 5 indices (sorted descending)
# top5_idx_sorted = segment_indices[np.argsort(segment_scores)[-5:]][::-1]
#
# for i, t_offset in enumerate(T_Offset):
#     next_p_onset = next((p for p in P_onset if p > t_offset), None)
#
#     if next_p_onset is not None:
#         if i in top5_idx_sorted:
#             seg_start = int(t_offset)
#             seg_end = int(next_p_onset)
#
# for i in range(len(d1) - 5, 5, -1):
#
#     # look for beginning of rapid upstroke
#     if d1[i] < slope_thr and np.mean(d1[i + 1:i + 6]) > slope_thr:
#         raw_segment = R_peak_ecg_signal[seg_start:seg_end]
#         seg_x = np.arange(seg_start, seg_end)
#
#         # -------------------------------------------------------
#         #        BANDPASS FILTER (30\u201380 Hz)
#         # -------------------------------------------------------
#         from scipy.signal import butter, filtfilt
#
#
#         def bandpass_filter(signal, fs, lowcut=30, highcut=100, order=4):
#             nyq = 0.5 * fs
#             low = lowcut / nyq
#             high = highcut / nyq
#             b, a = butter(order, [low, high], btype='band')
#             return filtfilt(b, a, signal)
#
#
#         if len(raw_segment) > 10:
#             filtered_segment = bandpass_filter(raw_segment, fs)
#
# if QRS_on is not None and best_idx < len(P_onset):
#
#     p_on = int(P_onset[best_idx])
#
#     if p_on < QRS_on:
#
#         seg_start = p_on
#         seg_end = QRS_on + 1
#
#         raw_segment = R_peak_ecg_signal[seg_start:seg_end]
#         seg_x = np.arange(seg_start, seg_end)
#
#         # -------------------------------
#         # BANDPASS (30\u2013100 Hz)
#         # -------------------------------
#         from scipy.signal import butter, filtfilt
#
#
#         def bandpass(signal, fs, lowcut=30, highcut=100, order=4):
#             nyq = 0.5 * fs
#             b, a = butter(order, [lowcut / nyq, highcut / nyq], btype='band')
#             return filtfilt(b, a, signal)
#
#
#         if len(raw_segment) > 10:
#             bp = bandpass(raw_segment, fs)
#
#             # Normalize + scale for visibility
#             bp_norm = bp / np.max(np.abs(bp))
#             scale = 0.5 * np.max(np.abs(raw_segment))
#             bp_scaled = bp_norm * scale
#
# # -------------------------------------------------------
# #        A WAVE DETECTION (FINAL - TRUE PEAK, NO SHIFT)
# # -------------------------------------------------------
# A_wave = None
# bp_segment = bp
# qrs_local = QRS_on - seg_start
# # Early PR region (tight window)
# search_start = 15
# search_end = int(0.45 * qrs_local)
# sub = bp_segment[search_start:search_end]
# if len(sub) > 5:
#     peak_idx = np.argmax(sub)  # TRUE peak (not first, not thresholded)
#     A_wave = seg_start + search_start + peak_idx
#
# # -------------------------------------------------------
# #        H_on and H_Off DETECTION (TRUE BIPHASIC START)
# # -------------------------------------------------------
#
# H_on = None  ### replace in main code  ####
# bp_segment = bp  # bandpassed signal
# qrs_local = QRS_on - seg_start
# # search region before QRS (where your circled wave is)
# search_start = max(0, qrs_local - 60)
# search_end = qrs_local - 5
# sub = bp_segment[search_start:search_end]
# # Step 1: find zero crossing (center of biphasic)
# zc_idx = None
# for i in range(1, len(sub)):
#     if sub[i - 1] < 0 and sub[i] > 0:
#         zc_idx = i
#         break
#
# # Step 2: go BACK to find beginning of that wave
# if zc_idx is not None:
#     for i in range(zc_idx, 1, -1):
#         # beginning = where slope starts increasing
#         if abs(sub[i] - sub[i - 1]) < 0.001:
#             H_on = seg_start + search_start + i
#             break
#
# # search between H_on and QRS
# if H_on is not None:
#     h_on_local = H_on - seg_start
#
#     search_start = h_on_local + 5
#     search_end = qrs_local - 2
#
#     sub = bp_segment[search_start:search_end]
#
#     if len(sub) > 5:
#         # find LAST zero-crossing before QRS
#         for i in range(len(sub) - 1, 1, -1):
#             if sub[i - 1] > 0 and sub[i] < 0:
#                 H_off = seg_start + search_start + i
#                 break  ### till here ###
#
# PA_ms = A_wave - p_on
# AH_ms = H_on - A_wave
# HV_ms = QRS_on - H_on
# PR_ms = QRS_on - p_on
# QRS_ms = QRS_off - QRS_on
#
#
# # PA_ms = int(PA_ms)
# # AH_ms = int(AH_ms)
# # HV_ms = int(HV_ms)
# # PR_ms = int(PR_ms)
# # QRS_ms = int(QRS_ms)
# #
# #     return PA_ms, AH_ms, HV_ms, PR_ms, QRS_ms
#
#
# def process(buffer2):
#     fs = 1000
#
#     v2 = np.array(buffer2, dtype=np.float64)
#
#     PA_ms, AH_ms, HV_ms, PR_ms, QRS_ms = analyze_his_bundle(v2)
#
#     return {
#         "PA": PA_ms,
#         "AH": AH_ms,
#         "HV": HV_ms,
#         "PR": PR_ms,
#         "QRS": QRS_ms
#     }




##########this code on 17/07/2026
## ============================================================
## FIXED VERSION 2
## Changes in this revision:
##   1. HH value removed everywhere (no longer computed, no
##      longer returned to Android, no longer in the debug table).
##   2. THE MAIN BUG: analyzelead4() -- the function Android
##      actually calls -- was NOT using the real PA/AH/HV
##      detection algorithm. It was using placeholder guesses:
##          AH_ms = PR_ms * 0.3
##          HV_ms = QRS_ms * 0.2
##      These are not measurements, just arbitrary fractions of
##      PR/QRS, which is why the on-device numbers were wrong.
##      The *real* algorithm (bandpass filter -> A-wave peak ->
##      H-on/H-off zero-crossing detection) only existed inside
##      process(), which the app never called.
##   3. Fix: the real algorithm from process() has been pulled
##      out into one shared function,
##      compute_his_bundle_intervals(), and BOTH process() and
##      analyzelead4() now call it. So the values Android
##      receives are the same real measurement, not a guess.
##   4. No thresholds, window sizes, filter cutoffs, or any other
##      numeric parameter/logic used by the real algorithm were
##      changed -- only removed the fake approximations and wired
##      up the real one instead.
##   5. R_Peak_Detection_05: fixed IndexError crash.
##      "len(max_peak) < 2" matched BOTH zero peaks found AND one
##      peak found, then unconditionally indexed max_peak[0] --
##      which throws IndexError on an empty array. find_peaks()
##      requires a true local max with lower neighbors on both
##      sides, so a flat/noisy 140-sample window can legitimately
##      return zero peaks. Changed to "== 1" so only the true
##      single-peak case indexes max_peak[0]; zero-peak and
##      multi-peak cases both fall back to argmax(seg), same as
##      the pre-existing multi-peak fallback. Applied to both the
##      R-peak block and the mirrored S-wave block.
##   6. R_Peak_Detection_05: replaced the hardcoded threshold
##      window max(data_S[1000:5000]) with a whole-buffer robust
##      percentile (99.5th) so short buffers, or buffers where
##      the first ~4s happens to be noisy/quiet, don't miscalibrate
##      the peak-detection threshold for the entire buffer.
## ============================================================
#
# import math
# import collections
# import datetime
# import numpy as np
# import scipy as sp
# from scipy import signal
# from scipy.signal import butter, filtfilt
# import statistics as st
# import pandas as pd
# from pytz import timezone
#
# try:
#     import condat_tv
# except ImportError:
#     condat_tv = None
#     print("WARNING: condat_tv not available on this device — "
#           "sparse/TV denoise step will be skipped for this run.")
#
#
# ## data loss correction ##
#
# def data_loss(input_signal):
#     for x in range(1, len(input_signal) - 1, 1):  #### data loss
#         if (input_signal[
#             x] == 0):  ## checks zero values in input signal and replaces with the mean of adjacent non zero values
#             input_signal[x] = st.mean([input_signal[x - 1], input_signal[x + 1]])
#     return (input_signal)
#
#
# ## time stamp ##
# def get_time_from_timestamp(
#         timestamp):  ## converting unit timestamp to a readable date and time format
#     read_able = datetime.datetime.fromtimestamp(timestamp)
#     now_asia = str(read_able.astimezone(timezone('Asia/Kolkata')));  # print(now_asia)
#     year = now_asia[0:10]
#     year = year[8:10] + "-" + year[5:7] + "-" + year[0:4]
#     time = now_asia[11:19]
#     return (year, time)
#
#
# ## function extracts values from a dictionary given a specific index ##
# def Value_axis(data_dict, value):
#     data = list(data_dict.items())
#     an_array = np.array(data, dtype=object)
#     Values_1 = an_array[value]
#     Values_2 = np.asarray(Values_1[1])
#     return Values_2
#
#
# ## smoothening filter: Baseline filter+powerline noise+150 hz low pass filter ##
# def ecg_filters_V5_smooth(lead_data,
#                           hp=0.67):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     for x in range(1, len(lead_data), 1):
#         if (lead_data[x] == 0):
#             lead_data[x] = lead_data[x - 1]
#
#     ##### Baseline filter without ST Segment distortion
#     b = signal.firwin(2377, cutoff=[hp / 500], window="hamming", pass_zero=False);
#     a = 1
#     # b = signal.firwin(1735, cutoff = [0.67/500], window = "hamming", pass_zero=False); a = 1
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     # h, g = signal.butter(2, [45/500] , btype='low', analog=False)
#     h, g = signal.butter(2, [45 / 500], btype='low', analog=False)
#     j, i = signal.butter(2, [15 / 500], btype='low', analog=False)
#
#     filt_BW = signal.filtfilt(b, a, lead_data);
#     filt_SM = (signal.lfilter(j, i, filt_BW))
#     filt_LP = (signal.lfilter(h, g, filt_BW))
#
#     sparse = filt_LP - filt_SM;
#     if condat_tv is not None:
#         denoise = condat_tv.tv_denoise(sparse, 6.5)
#     else:
#         denoise = sparse  # fallback if the native lib isn't bundled on device
#     smooth_data = filt_SM + denoise
#
#     return smooth_data
#
#
# def baseline_filter(lead_data, hp=0.67):
#     ##### Baseline filter without ST Segment distortion
#     b = signal.firwin(2377, cutoff=[hp / 500], window="hamming", pass_zero=False);
#     a = 1
#     filt_BW = signal.filtfilt(b, a, lead_data);
#     return filt_BW
#
#
# def ecg_filters_V5_01(lead_data):
#     """
#     ECG Filtering according to IEC 60601-2-25
#     - Baseline wander removal
#     - 50 Hz notch
#     - 100 Hz notch
#     - 45 Hz low-pass
#     - Optional Savitzky-Golay smoothing
#
#     NOTE: replaces the previous hand-rolled powerline-notch coefficients
#     (applied via signal.lfilter) with signal.iirnotch + filtfilt. This
#     is functionally the same specification (50Hz/100Hz notch, 45Hz
#     low-pass, same ~1Hz baseline high-pass cutoff) but zero-phase
#     throughout, instead of the mix of filtfilt (baseline) + lfilter
#     (notch/low-pass) the previous version used. lfilter introduces a
#     real group delay; filtfilt does not. This also reduces the phase
#     offset this signal previously had relative to other pass-1
#     computations that assumed lead_data timing was preserved.
#     """
#
#     lead_data = np.asarray(lead_data, dtype=float).copy()
#
#     # Replace missing (zero) samples
#     for i in range(1, len(lead_data)):
#         if lead_data[i] == 0:
#             lead_data[i] = lead_data[i - 1]
#
#     Fs = 1000.0
#
#     # ------------------------------------------------------------------
#     # Baseline Wander Removal (High-pass FIR)
#     # ------------------------------------------------------------------
#     hp_coeff = signal.firwin(
#         numtaps=2377,
#         cutoff=1,
#         fs=Fs,
#         pass_zero=False,
#         window="hamming"
#     )
#
#     baseline_removed = signal.filtfilt(hp_coeff, [1.0], lead_data)
#
#     # ------------------------------------------------------------------
#     # 50 Hz Notch Filter
#     # ------------------------------------------------------------------
#     notch50_b, notch50_a = signal.iirnotch(
#         w0=50,
#         Q=30,
#         fs=Fs
#     )
#
#     filtered = signal.filtfilt(notch50_b, notch50_a, baseline_removed)
#
#     # ------------------------------------------------------------------
#     # 100 Hz Notch Filter
#     # ------------------------------------------------------------------
#     notch100_b, notch100_a = signal.iirnotch(
#         w0=100,
#         Q=30,
#         fs=Fs
#     )
#
#     filtered = signal.filtfilt(notch100_b, notch100_a, filtered)
#
#     # ------------------------------------------------------------------
#     # 45 Hz Low-pass Butterworth
#     # ------------------------------------------------------------------
#     lp_b, lp_a = signal.butter(
#         N=4,
#         Wn=45,
#         btype='low',
#         fs=Fs
#     )
#
#     filtered = signal.filtfilt(lp_b, lp_a, filtered)
#
#     # ------------------------------------------------------------------
#     # Optional smoothing
#     # ------------------------------------------------------------------
#     filtered = signal.savgol_filter(
#         filtered,
#         window_length=29,
#         polyorder=3
#     )
#
#     return filtered
#
#
# def phasor_transform(signal_in, Rv):
#     PT = np.empty_like(signal_in, dtype=float)
#     for i in range(len(signal_in)):
#         ch = signal_in[i] / Rv
#         PT[i] = math.degrees(math.atan(ch))
#         # PT[i] = Rv+ ()
#     return (PT)
#
#
# def find_PVC(signal_in, Rpeaks, Ramplitued, RRinterval):
#     ##   LIST OF AREA UNDER THE CURVE
#     AUC = collections.deque()
#     n_sig = len(signal_in)
#     for k in Rpeaks:
#         # Guard against negative slice start wrapping around to the END
#         # of the array (Python silently allows signal_in[-5:10], which
#         # silently corrupts the AUC/PVC score for early beats instead
#         # of raising an error).
#         lo = max(0, k - 80)
#         hi = min(n_sig, k + 80)
#         ch = abs(np.trapz(signal_in[lo:hi]));
#         AUC.append(ch)
#         # print("median : ",ch, 1.3*st.median(AUC))
#     AUC = np.array(AUC);  # print(len(AUC),AUC)
#     ##    ECTOPIC FUNCTION
#     PVC_list = np.empty_like(Rpeaks, dtype=float);
#     PVC_list[:] = np.nan
#     SVC_list = np.empty_like(Rpeaks, dtype=float);
#     SVC_list[:] = np.nan
#     for i in range(1, len(RRinterval)):
#         if RRinterval[i] >= (1.5 * RRinterval[i - 1]):
#             if Ramplitued[i] >= (Ramplitued[i - 1] + (0.2 * Ramplitued[i - 1])):  # PVC
#                 # Guard: at i == 1, AUC[0:i-1] == AUC[0:0] == empty,
#                 # and statistics.median() raises StatisticsError on an
#                 # empty sequence. There's no preceding-beat baseline to
#                 # compare against yet, so skip the PVC check for the
#                 # very first comparable beat instead of crashing the
#                 # whole buffer's analysis.
#                 if i > 1 and AUC[i] > (1.3 * st.median(AUC[0:i - 1])):
#                     PVC_list[i] = Rpeaks[i]
#             else:  # SVC or PVC
#                 if i > 1:
#                     if AUC[i] > (1.3 * st.median(AUC[0:i - 1])):
#                         PVC_list[i] = Rpeaks[i]
#     return np.array(PVC_list)
#
#
# def ecg_filters_Pwave(
#         lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     h, g = sp.signal.butter(4, [40 / 500], btype='low', analog=False);
#     j, i = signal.butter(2, [15 / 500], btype='low', analog=False)
#     filt_50 = sp.signal.lfilter(d, c, lead_data);
#     filt_100 = sp.signal.lfilter(f, e, filt_50)
#     filt_LP = sp.signal.lfilter(h, g, filt_50)
#     smoothed = sp.signal.savgol_filter(filt_LP, window_length=19, polyorder=3)
#     n = 41;
#     baseline_wander = sp.signal.medfilt(smoothed, kernel_size=n);
#     return baseline_wander
#
#
# def calculate_p_offset_p_duration_and_average_pr(p_loc, r_peak_ecg_signal, qrs_onset,
#                                                  window_size=60):
#     p_offset = []
#     p_duration = []
#     pr_differences = []
#
#     for n in range(len(p_loc)):
#         loc2 = p_loc[n] + window_size
#         p_start = r_peak_ecg_signal[loc2]
#         p_off = r_peak_ecg_signal[loc2 + 1]
#         count = 1
#
#         while p_start < p_off:
#             p_start = r_peak_ecg_signal[loc2 + count]
#             p_off = r_peak_ecg_signal[loc2 + 1 + count]
#             count = count + 1
#
#         p_offset.append(loc2 + count)
#         p_duration.append(qrs_onset[n] - (p_loc[n] - count))
#         pr_differences.append(qrs_onset[n] - (loc2 + count))
#
#     average_p_duration = np.mean(np.subtract(p_offset, p_loc))
#     average_pr = np.mean(pr_differences) + 80
#     if average_pr < 80:
#         average_pr = average_pr + 100
#     if average_pr < 0:
#         average_pr = average_pr + 150
#
#     return p_offset, p_duration, average_pr
#
#
# def detect_qrs_onset_offset(r_location, r_peak_ecg_signal, onset_window=50,
#                             offset_window=50):  ### onset offset change into 80 80 when bundle brunch block
#     qrs_onset = []
#     qrs_offset = []
#     signal_len = len(r_peak_ecg_signal)
#
#     for m in range(len(r_location)):
#         # QRS Onset
#         loc = r_location[m] - onset_window
#
#         if loc <= 0:
#             # Guard: negative loc would silently wrap around and index
#             # from the END of the array instead of erroring, corrupting
#             # this beat (and anything downstream, e.g. Mode_Value).
#             qrs_onset.append(0)
#         else:
#             R_peak = r_peak_ecg_signal[loc]
#             R_prev = r_peak_ecg_signal[loc - 1]
#             count = 1
#
#             while R_peak > R_prev and (loc - count - 1) >= 0:
#                 R_peak = r_peak_ecg_signal[loc - count]
#                 R_prev = r_peak_ecg_signal[loc - count - 1]
#                 count = count + 1
#
#             qrs_onset.append(max(0, loc - count))
#
#         # QRS Offset
#         loc2 = r_location[m] + offset_window
#
#         if loc2 >= signal_len - 1:
#             qrs_offset.append(signal_len - 1)
#         else:
#             R_on = r_peak_ecg_signal[loc2]
#             R_end = r_peak_ecg_signal[loc2 + 1]
#             count = 1
#
#             while R_on < R_end and (loc2 + count + 1) < signal_len:
#                 R_on = r_peak_ecg_signal[loc2 + count]
#                 R_end = r_peak_ecg_signal[loc2 + 1 + count]
#                 count = count + 1
#
#             qrs_offset.append(min(signal_len - 1, loc2 + count))
#
#     return qrs_onset, qrs_offset
#
#
# def calculate_pr_interval(r_location, qrs_onset, ecg_signal, offset_value=80):
#     r_loc_new = np.empty(len(r_location), dtype=int)
#     pr_array = np.empty(len(r_location))
#
#     for v in range(len(r_location)):
#         r_loc_new[v] = r_location[v] - offset_value
#         pr_array[v] = np.mean(ecg_signal[(r_loc_new[v]):(qrs_onset[v])])
#
#     return r_loc_new, pr_array
#
#
# def calculate_mode(pr_array):
#     mode_value = st.mode(pr_array)
#     return mode_value
#
#
# def calculate_new_qrs_onset_offset(r_location, r_peak_ecg_signal, mode_value, offset_before=50,
#                                    offset_after=50):  ###  offset before and offset after  make it 80 80 if bundle bunch block
#     new_qrs_onset = []
#     new_qrs_offset = []
#     signal_len = len(r_peak_ecg_signal)
#
#     for a in range(len(r_location)):
#         # QRS Onset
#         loc = r_location[a] - offset_before
#         if loc <= 0:
#             new_qrs_onset.append(0)
#         else:
#             R_peak = r_peak_ecg_signal[loc]
#             R_prev = r_peak_ecg_signal[loc - 1]
#             count = 1
#             while R_peak > R_prev > mode_value and (loc - count - 1) >= 0:  # Added boundary check
#                 R_peak = r_peak_ecg_signal[loc - count]
#                 R_prev = r_peak_ecg_signal[loc - count - 1]
#                 count = count + 1
#             new_qrs_onset.append(max(0, loc - count + 1))  # Adjusted for consistency
#
#     for b in range(len(r_location)):
#         # QRS Offset
#         loc2 = r_location[b] + offset_after
#         if loc2 >= signal_len - 1:  # If loc2 is already at or beyond the last valid index, the offset is the last index.
#             new_qrs_offset.append(signal_len - 1)
#         else:
#             # Check loc2 + 1 before accessing it in r_end
#             if loc2 + 1 >= signal_len:
#                 new_qrs_offset.append(
#                     signal_len - 1)  # If loc2 is valid but loc2+1 is not, offset is the last index
#             else:
#                 r_on = r_peak_ecg_signal[loc2]
#                 r_end = r_peak_ecg_signal[loc2 + 1]
#                 count = 1
#                 while r_on < r_end < mode_value and (
#                         loc2 + count + 1) < signal_len:  # Added boundary check
#                     r_on = r_peak_ecg_signal[loc2 + count]
#                     r_end = r_peak_ecg_signal[loc2 + 1 + count]
#                     count = count + 1
#                 new_qrs_offset.append(
#                     min(signal_len - 1, loc2 + count - 1))  # Adjusted for consistency
#
#     return new_qrs_onset, new_qrs_offset
#
#
# def R_Peak_Detection_05(raw_data):
#     peaks_up = [];
#     amp = [];
#     peaks_up_s = [];
#     ecp = [];
#     b = signal.firwin(1377, cutoff=[2 / 500], window="hamming", pass_zero=False);
#     a = 1
#     h, g = signal.butter(4, [45 / 500], btype='low', analog=False)
#     filt_BW = signal.filtfilt(b, a, raw_data);
#     data_F = signal.lfilter(h, g, filt_BW)
#     data_D = np.diff(data_F);
#     data_S = data_D * data_D
#     # Robust whole-buffer threshold instead of a hardcoded [1000:5000]
#     # window. The old fixed window assumed the first ~4 seconds of the
#     # buffer were always representative of QRS amplitude - if that
#     # window happened to contain noise/motion artifact (or was unusually
#     # quiet) while the rest of the buffer was clean, the threshold was
#     # miscalibrated for the ENTIRE buffer. A high percentile over the
#     # whole signal is robust to a few extreme edge-filter-transient
#     # samples while still scaling with genuine QRS amplitude, and works
#     # regardless of buffer length (1s, 15s, or otherwise).
#     robust_peak_ref = np.percentile(data_S, 99.5)
#     peaks, rpeak = signal.find_peaks(data_S, distance=255, height=(robust_peak_ref / 1.7));
#     n = len(peaks);
#
#     for i in range(0, n):
#         win = 70;
#         if (peaks[i] - (win)) < 0:
#             start_win = 0
#         else:
#             start_win = (peaks[i] - (win))
#         seg = data_F[start_win:(peaks[i]) + (
#             win)];  # seg = seg*seg; ############## Multiple Seg for getting the R or S peak
#         max_peak, peak_amp = signal.find_peaks(seg, distance=10, height=(max(seg) / 2))
#         # NOTE: "len(max_peak) < 2" also matches ZERO peaks found, and
#         # max_peak[0] then crashes with IndexError on an empty array.
#         # find_peaks() requires a true local max with lower neighbors on
#         # both sides, so a peak sitting right at the edge of this window
#         # (or a flat/noisy segment) can legitimately return zero peaks.
#         # Only trust max_peak[0] when exactly one peak was found; zero
#         # or multiple peaks both fall back to a plain argmax of the
#         # segment, same as the pre-existing multi-peak fallback below.
#         if len(max_peak) == 1:
#             peaks_u = start_win + max_peak[0]
#         else:
#             peaks_u = start_win + np.argmax(seg)
#         peaks_up.append(peaks_u);
#
#         seg_s = seg * (-1);
#         max_peak, peak_amp = signal.find_peaks(seg_s, distance=10)  # , height = (max(seg_s)/2))
#         if len(max_peak) == 1:
#             peaks_u = start_win + max_peak[0]  ########### need fix for short S wave
#         else:
#             peaks_u = start_win + np.argmax(seg_s)
#         peaks_up_s.append(peaks_u);
#
#     rr_int = np.diff(peaks_up);
#     th_ep = st.mean(rr_int) + st.mean(rr_int) / 10
#
#     for j in range(0, len(rr_int)):
#         if rr_int[j] > th_ep:
#             ep_seg = (data_F[peaks_up[j] + 60:peaks_up[j + 1] - 60])
#             max_e = np.argmax(ep_seg * ep_seg)  ########### for irregularity needs to be fixed
#             max_e = max_e + peaks_up[j] + 60;
#             # if ep_data[max_e] > (data_F[peaks_up[j]]/2):
#             ecp.append(max_e)
#     r_peak_values = [data_F[peak] for peak in peaks_up]
#     return peaks_up, ecp, peaks_up_s
#
#
# def P_Detection(rawECG, R_peaks, RRint, Ramp):
#     Filt_ECG = ecg_filters_V5_smooth(rawECG)
#     peaks = R_peaks;
#
#     PVC = find_PVC(Filt_ECG, peaks, Ramp, RRint)
#     PT_pwave = phasor_transform(ecg_filters_Pwave(rawECG), Rv=0.05)
#     P_st = [];
#     P_sp = [];
#     loc = [];
#     Pamp = []
#     P_location = [];
#     Pst = [];
#     Psp = []
#     for k in range(0, len(peaks)):
#         if k == 0:  # 1st P-peak detection
#             win_1 = len(Filt_ECG[0:peaks[0]]);  # print(win_1)
#             if win_1 > 300:
#                 win_st = peaks[0] - 300;
#                 win_sp = peaks[0] - 80
#                 ch = np.max(PT_pwave[peaks[0] - 300:peaks[0] - 80]);
#             elif win_1 <= 300 and win_1 >= 80:
#                 win_st = 0;
#                 win_sp = peaks[0] - 80
#                 ch = np.max(PT_pwave[0:peaks[0] - 80]);
#             elif win_1 < 80:
#                 P_location.append(np.nan);
#                 win_st = np.nan;
#                 win_sp = np.nan
#                 break;
#         elif k > 0 and k < len(peaks) - 1:  # Second to Last but 1 peak
#             win_st = int(peaks[k] - (0.4 * RRint[k]));
#             win_sp = int(peaks[k] - (0.05 * (RRint[k] - 100)));  # print(win_st,win_sp)
#             ch = np.max(PT_pwave[win_st:win_sp]);
#         else:  # Last peak
#             win_st = int(peaks[k] - (0.4 * RRint[k - 1]));
#             win_sp = int(peaks[k] - (0.05 * (RRint[k - 1] - 100)));  # print(win_st,win_sp)
#             ch = np.max(PT_pwave[win_st:win_sp]);
#         loc_ch = (np.where(PT_pwave == ch));
#         loc_ch = loc_ch[0];
#         if loc_ch[0] < 80:
#             loc_st = loc_ch[0]
#         else:
#             loc_st = loc_ch[0] - 80
#         ix = np.max(Filt_ECG[loc_st:loc_ch[0] + 80]);
#         loc_ch = (np.where(Filt_ECG == ix));
#         loc_ch = loc_ch[0];
#         loc.append(loc_ch[0]);
#         Pamp.append(np.abs(Filt_ECG[loc_ch[0]]))
#         P_st.append(win_st);
#         P_sp.append(win_sp)
#
#     # print("Pamp temp :", len(Pamp), Pamp);  print("Ploc temp :", len(loc), loc)
#
#     for m in range(0, len(RRint)):
#         # print(m)
#         if np.isnan(PVC[m]) == True and Pamp[m] > (
#                 0.5 * Filt_ECG[peaks[m]]):  # Beat Not a PVC: retain P loc
#             P_location.append(loc[m])
#             Pst.append(P_st[m]);
#             Psp.append(P_sp[m])
#
#     P_location = loc
#     P_onset = [];  # print("P location : ",P_location)
#     # print("Pst :", Pst); print("Psp :",Psp)
#     for m in range(len(P_location)):
#         loc = P_location[m]
#         T_on = Filt_ECG[loc - 0];
#         prev = Filt_ECG[loc - 20 - 1];
#         Count = 1
#         while T_on > prev:
#             T_on = Filt_ECG[loc - 0 - Count];
#             prev = Filt_ECG[loc - 20 - Count - 1];
#             Count = Count + 1
#         P_onset.append(loc - Count);
#     return (P_location, Pamp, P_onset)
#
#
# def ecg_filters_Twave(
#         lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     h, g = sp.signal.butter(4, [20 / 500], btype='low', analog=False)
#     j, i = sp.signal.butter(2, [15 / 500], btype='low', analog=False)
#     filt_50 = sp.signal.lfilter(d, c, lead_data);  # filt_100 = sp.signal.lfilter(f, e, filt_50)
#     filt_LP = sp.signal.lfilter(h, g, filt_50)
#     smoothed = sp.signal.savgol_filter(filt_LP, window_length=19, polyorder=3)
#     n = 41;
#     baseline_wander = sp.signal.medfilt(smoothed, kernel_size=n);
#     return baseline_wander
#
#
# def T_Detection(raw_signal, R_peaks, RRint):
#     Filt_ECG = ecg_filters_V5_smooth(raw_signal)
#     T_filt = ecg_filters_Twave(raw_signal)
#     PT_twave = phasor_transform(T_filt, Rv=0.1)
#     T_st = [];
#     T_sp = [];
#     T_loc = [];
#     Tamp = []
#     for k in range(0, len(R_peaks)):
#         if k == len(R_peaks) - 1:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k - 1]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k - 1])));  # print(win_st,win_sp)
#         elif k > 0 and k < len(R_peaks) - 1:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k])));  # print(win_st,win_sp)
#         else:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k + 1]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k + 1])));  # print(win_st,win_sp)
#         # Check if the slice is empty and handle it.
#         # win_st < win_sp as raw integers does NOT guarantee a non-empty
#         # slice: if both fall past the end of the buffer (common for the
#         # last beat, whose window is sized off RRint[k-1]), Python
#         # silently truncates PT_twave[win_st:win_sp] to an empty array
#         # instead of raising - and np.max() on an empty array throws
#         # "zero-size array to reduction operation maximum which has no
#         # identity", which crashed the ENTIRE buffer's analysis (the
#         # exception propagates out of T_Detection -> compute_his_bundle_
#         # intervals -> analyzelead4's except block -> all-zero result).
#         # Clamp both bounds to the signal length first so the emptiness
#         # check reflects the slice that will actually be taken.
#         sig_len = len(PT_twave)
#         win_st_c = max(0, min(win_st, sig_len))
#         win_sp_c = max(0, min(win_sp, sig_len))
#         if win_st_c >= win_sp_c:
#             print(f"Warning: Empty slice for R-peak index {k}. win_st: {win_st}, win_sp: {win_sp}")
#             # Skip this iteration if the slice is empty
#             continue
#         else:
#             ch = np.max(PT_twave[win_st_c:win_sp_c])
#         loc_ch = (np.where(PT_twave == ch));
#         loc_ch = loc_ch[0];
#         # print("window :",loc_ch[0]-80,loc_ch[0]+80)
#
#         # Check if loc_ch is empty before indexing
#         if len(loc_ch) > 0:
#             ix = np.max(Filt_ECG[loc_ch[0] - 80:loc_ch[0] + 80]);
#             loc_ch = (np.where(Filt_ECG == ix));
#             loc_ch = loc_ch[0];
#             T_loc.append(loc_ch[0]);
#             Tamp.append(np.abs(Filt_ECG[loc_ch[0]]))
#             T_st.append(win_st);
#             T_sp.append(win_sp)
#         else:
#             print(f"Warning: loc_ch is empty for R-peak index {k}. Skipping this iteration.")
#             # Handle the case where loc_ch is empty, e.g., append NaN to T_loc, etc.
#
#     return (np.array(T_loc))
#
#
# def calculate_t_offset(t_loc, r_peak_ecg_signal):
#     t_offset = []
#     signal_length = len(r_peak_ecg_signal)
#
#     for o in range(len(t_loc)):
#         location = t_loc[o]
#         t_start = r_peak_ecg_signal[location]
#         t_off = r_peak_ecg_signal[min(location + 1, signal_length - 1)]
#         count = 1
#
#         while t_start < t_off and location + count + 1 < signal_length:
#             t_start = r_peak_ecg_signal[min(location + count, signal_length - 1)]
#             t_off = r_peak_ecg_signal[min(location + count + 1, signal_length - 1)]
#             count += 1
#
#         # Ensure the offset doesn't go out of bounds
#         final_offset = min(location + count + 100, signal_length - 1)
#         t_offset.append(final_offset)
#
#     return t_offset
#
#
# def calculate_snr_db(raw_signal, filtered_signal):
#     """
#     Whole-buffer SNR: compares the raw (unfiltered) signal against the
#     smoothed/filtered signal, treating the filtered signal as "signal"
#     and whatever the filter removed (raw - filtered) as "noise".
#
#     NOTE: this is an inferred standard implementation. The actual
#     calculate_snr_db() from the Colab reference notebook was not
#     provided - if it computes SNR differently, swap this out with the
#     real definition.
#     """
#     signal_power = np.mean(filtered_signal ** 2)
#     noise = raw_signal - filtered_signal
#     noise_power = np.mean(noise ** 2)
#
#     eps = 1e-12
#     if noise_power > eps:
#         return 10 * np.log10(max(signal_power, eps) / noise_power)
#     else:
#         return float("inf")
#
#
# def bandpass(signal_in, fs, lowcut=30, highcut=100, order=4):
#     # Matched exactly to the Colab reference implementation
#     # (def bandpass(signal, fs, lowcut=30, highcut=100, order=4)).
#     # A previous revision of this file used highcut=499.9, reasoning
#     # that a narrower 30-100Hz band was distorting the A-wave/H-onset
#     # deflections - that reasoning was never validated against the
#     # actual Colab reference and contradicted it, so it has been
#     # reverted back to the validated 30-100Hz band.
#     nyq = 0.5 * fs
#     b, a = butter(order, [lowcut / nyq, highcut / nyq], btype='band')
#     return filtfilt(b, a, signal_in)
#
#
# # ================================================================
# #   SHARED REAL ALGORITHM
# #   This is the single source of truth for PA / AH / HV / PR /
# #   QRS / SNR. Both process() (console/debug) and analyzelead4()
# #   (called from Android) call this so they can never drift out
# #   of sync again.
# # ================================================================
#
# def compute_his_bundle_intervals(v2, fs=1000, lsb_V=286e-9):
#     """
#     v2: already-scaled, already-inverted lead signal in mV, i.e.
#         v2 = (raw_samples * 0.000286) * (-gain)
#     Returns a dict with PA, AH, HV, PR, QRS (all ms) and SNR (dB).
#     Raises ValueError if a clean best-beat/segment could not be
#     resolved from this buffer (e.g. buffer too short / too noisy).
#     """
#
#     ###--------------------------- Raw_data fetching and R peak detection ----------------------------###
#     R_data = v2 * (-1)
#     R_peak_ecg_signal = ecg_filters_V5_01(R_data)
#     R_Location, ectopic, S_Location = R_Peak_Detection_05(R_data)
#     R_Location = np.array(R_Location, dtype=int)
#
#     # Keep this PASS-1 signal and R-peak positions under their own names.
#     # QRS_Onset/New_QRS_Onset/P_onset below are all computed against
#     # THIS signal/these positions. A second R-peak detection pass runs
#     # later (on a differently-filtered signal, for T-wave localization)
#     # and used to silently overwrite R_peak_ecg_signal/R_Location here -
#     # but ecg_filters_V5_01 and ecg_filters_V5_smooth use lfilter
#     # internally (not filtfilt), which introduces a real phase/group
#     # delay. In testing this produced a consistent ~6-sample offset
#     # between the "same" R-peak across the two passes. Measuring a beat
#     # (_evaluate_candidate_beat) by slicing the SECOND pass's signal
#     # using window boundaries computed from the FIRST pass's signal
#     # silently shifts the P-onset->QRS-onset segment by that offset -
#     # small in absolute terms, but large relative to the ~55-95 sample
#     # H-onset search window, and a plausible dominant cause of
#     # intermittently wrong/zero AH (and possibly HV) values with no
#     # error ever being raised. Fix: always measure a beat using the
#     # SAME signal/positions its P-onset and QRS-onset were derived from.
#     R_peak_ecg_signal_meas = R_peak_ecg_signal
#     R_Location_meas = R_Location
#
#     if len(R_Location) < 3:
#         raise ValueError("Not enough R peaks detected in this buffer.")
#
#     ###--------------------------Heart Rate calculation---------------------------------- ###
#     R_amp = [];
#     RRint = [];
#     for i in range(0, len(R_Location) - 1):
#         ch = R_Location[i + 1] - R_Location[i];
#         RRint.append(ch)
#         R_amp.append(R_peak_ecg_signal[R_Location[i]])
#         if i == (len(R_Location) - 2):
#             R_amp.append(R_peak_ecg_signal[R_Location[i + 1]])
#
#     QRS_Onset, QRS_Offset = detect_qrs_onset_offset(R_Location, R_peak_ecg_signal)
#     R_Loc_New, PR_Array = calculate_pr_interval(R_Location, QRS_Onset, R_peak_ecg_signal)
#     Mode_Value = calculate_mode(PR_Array)
#     New_QRS_Onset, New_QRS_Offset = calculate_new_qrs_onset_offset(R_Location, R_peak_ecg_signal,
#                                                                    Mode_Value)
#     P_location, Pamp, P_onset = P_Detection(R_data, R_Location, RRint, R_amp)
#
#     filtered_v2 = ecg_filters_V5_smooth(v2)
#     R_data = filtered_v2 * (-1)
#
#     # ---------- Whole-buffer SNR ----------
#     # Matched to the Colab reference: compares the ORIGINAL raw signal
#     # against the SMOOTHED signal across the entire buffer, not just
#     # the one selected beat's tiny His-bundle window. This measures
#     # overall recording quality rather than one beat's local quality.
#     raw_signal = v2 * (-1)
#     filtered_signal = R_data
#     SNR_dB = calculate_snr_db(raw_signal, filtered_signal)
#
#     R_peak_ecg_signal = ecg_filters_V5_smooth(R_data)
#     R_Location, ectopic, S_Location = R_Peak_Detection_05(R_data)
#     T_loc = T_Detection(R_peak_ecg_signal, R_Location, RRint)
#     T_Offset = calculate_t_offset(T_loc, R_peak_ecg_signal)
#
#     T_Offset = np.array(T_Offset, dtype=int)
#     P_location = np.array(P_location, dtype=int)
#     P_onset = np.array(P_onset, dtype=int)
#
#     ads_values_tloc_p_location = []
#     for i, t_offset in enumerate(T_Offset):
#         next_p_onset = next((p for p in P_onset if p > t_offset), None)
#         if next_p_onset is not None and next_p_onset < len(R_peak_ecg_signal):
#             seg_start = int(t_offset)
#             seg_end = int(next_p_onset)
#             ads_segment = np.round(np.abs(R_peak_ecg_signal[seg_start:seg_end] / 0.000286)).astype(
#                 int)
#             ads_values_tloc_p_location.append(ads_segment)
#
#     if not ads_values_tloc_p_location:
#         raise ValueError("No T→P segments found for ADS analysis.")
#
#     # -------------------------------------------------------------------------
#     #                SELECT BEST 5 SEGMENTS (by RMS voltage)
#     # -------------------------------------------------------------------------
#     segment_scores = []
#     segment_indices = []
#
#     for i, ads_segment in enumerate(ads_values_tloc_p_location):
#         ads_segment = np.asarray(ads_segment, dtype=float)
#         if len(ads_segment) < 10:  # ignore very small segments
#             continue
#         voltage = ads_segment * lsb_V
#         rms = np.sqrt(np.mean(voltage ** 2))
#         segment_scores.append(rms)
#         segment_indices.append(i)
#
#     segment_scores = np.array(segment_scores)
#     segment_indices = np.array(segment_indices)
#
#     if len(segment_indices) == 0:
#         raise ValueError("No valid segments (all shorter than 10 samples) for ADS analysis.")
#
#     # Matched to the validated Colab reference implementation: sort
#     # DESCENDING and take the highest-RMS segments first.
#     #
#     # NOTE: an earlier revision of this file inverted this to
#     # ascending/lowest-RMS, reasoning that a flat T->P baseline should
#     # indicate a cleaner beat. That reasoning turned out to be wrong -
#     # the user's Colab notebook (validated against known-correct
#     # values) uses this original descending/highest-RMS-first order,
#     # so it has been reverted to match.
#     sorted_order = np.argsort(segment_scores)[::-1]  # descending: highest RMS first
#     candidate_indices = segment_indices[sorted_order]  # ALL candidates, best (highest RMS) first
#
#     # Try every candidate beat, best (quietest baseline) first, and use
#     # the first one that actually passes validation. The old code only
#     # ever tried the single top-ranked candidate and raised/crashed the
#     # ENTIRE buffer's analysis if that one beat happened to fail a
#     # validity check (e.g. "P-onset is not before QRS-onset") - even
#     # though 4+ other perfectly usable candidate beats had already been
#     # computed and were sitting right there unused.
#     last_error = None
#     result_core = None
#
#     for candidate_idx in candidate_indices:
#         try:
#             result_core = _evaluate_candidate_beat(
#                 candidate_idx,
#                 P_onset=P_onset,
#                 R_Location=R_Location_meas,
#                 New_QRS_Onset=New_QRS_Onset,
#                 New_QRS_Offset=New_QRS_Offset,
#                 R_peak_ecg_signal=R_peak_ecg_signal_meas,
#                 fs=fs,
#             )
#             break
#         except ValueError as e:
#             last_error = e
#             continue
#
#     if result_core is None:
#         raise ValueError(
#             f"No usable beat found among {len(candidate_indices)} candidates "
#             f"(last error: {last_error})."
#         )
#
#     # Apply the whole-buffer SNR (matched to Colab) computed earlier,
#     # rather than a per-beat value - _evaluate_candidate_beat leaves
#     # "SNR" as None on purpose since this is the single source of truth.
#     result_core["SNR"] = float(SNR_dB)
#
#     return result_core
#
#
# def _evaluate_candidate_beat(
#         candidate_idx,
#         P_onset,
#         R_Location,
#         New_QRS_Onset,
#         New_QRS_Offset,
#         R_peak_ecg_signal,
#         fs,
# ):
#     """
#     Attempt to compute PA/AH/HV/PR/QRS/SNR for ONE candidate beat.
#     Raises ValueError if this particular beat doesn't pass validation
#     (caller is expected to try the next-best candidate in that case).
#     """
#
#     if candidate_idx >= len(P_onset) or candidate_idx >= len(R_Location):
#         raise ValueError("Candidate segment index out of range for P_onset / R_Location.")
#
#     R_peak_i = int(R_Location[candidate_idx])
#
#     # ---------- QRS onset ----------
#     qrs_candidates = [q for q in New_QRS_Onset if q < R_peak_i]
#     if len(qrs_candidates) == 0:
#         raise ValueError("No QRS onset found before this candidate's R peak.")
#     QRS_on_i = qrs_candidates[-1]
#
#     # ---------- QRS offset ----------
#     qrs_off_candidates = [q for q in New_QRS_Offset if q > R_peak_i]
#     if len(qrs_off_candidates) == 0:
#         raise ValueError("No QRS offset found after this candidate's R peak.")
#     QRS_off_i = qrs_off_candidates[0]
#
#     # ---------- P onset for this beat ----------
#     p_on_i = int(P_onset[candidate_idx])
#     if p_on_i >= QRS_on_i:
#         raise ValueError("P-onset is not before QRS-onset for this candidate beat.")
#
#     # ---------- Bandpass the P-onset -> QRS-onset segment ----------
#     seg_start = p_on_i
#     seg_end = QRS_on_i + 1
#     raw_segment = R_peak_ecg_signal[seg_start:seg_end]
#
#     if len(raw_segment) <= 10:
#         raise ValueError("P-onset to QRS-onset segment too short to bandpass filter.")
#
#     bp = bandpass(raw_segment, fs)
#
#     # ---------- Scaled bandpassed trace for graphing ----------
#     # Matches Colab's bp_scaled: normalize the bandpassed (His-Purkinje)
#     # component to +-1 and rescale it to roughly the same visual
#     # amplitude as the raw segment, purely so it can be overlaid on the
#     # raw trace and stay visible instead of looking flat next to the
#     # much larger QRS/P-wave amplitudes.
#     bp_abs_max = np.max(np.abs(bp))
#     if bp_abs_max > 0:
#         bp_norm = bp / bp_abs_max
#         scale = 0.5 * np.max(np.abs(raw_segment))
#         bp_scaled = bp_norm * scale
#     else:
#         bp_scaled = bp
#
#     # ---------- A wave (true peak in early PR region) ----------
#     qrs_local = QRS_on_i - seg_start
#     search_end = int(0.45 * qrs_local)
#     sub = bp[15:search_end]
#     if len(sub) == 0:
#         raise ValueError("A-wave search window is empty for this candidate beat.")
#     peak_idx = np.argmax(sub)
#     A_wave_i = seg_start + 15 + peak_idx
#
#     # -------------------------------------------------------
#     #        H_on and H_off DETECTION (TRUE BIPHASIC START)
#     # -------------------------------------------------------
#     H_on_i = None
#     bp_segment = bp  # bandpassed signal
#
#     # search region before QRS (where the circled wave is)
#     search_start = max(0, qrs_local - 60)
#     search_end = qrs_local - 5
#     sub = bp_segment[search_start:search_end]
#
#     # Step 1: find zero crossing (center of biphasic)
#     zc_idx = None
#     for i in range(1, len(sub)):
#         if sub[i - 1] < 0 and sub[i] > 0:
#             zc_idx = i
#             break
#
#     # Step 2: go BACK to find beginning of that wave.
#     # Matched exactly to the Colab reference's fixed 0.001 flatness
#     # threshold. A previous revision of this file made this adaptive
#     # (scaled to each beat's own amplitude) to reduce how often H_on_i
#     # went undetected - but that deviated from the validated Colab
#     # implementation without device data to justify it. Reverted to
#     # match Colab exactly; see the fallback behavior below for the
#     # correct way Colab itself handles a genuine non-detection.
#     if zc_idx is not None:
#         for i in range(zc_idx, 1, -1):
#             # beginning = where slope starts increasing
#             if abs(sub[i] - sub[i - 1]) < 0.001:
#                 H_on_i = seg_start + search_start + i
#                 break
#
#     # search between H_on and QRS
#     H_off_i = None
#     if H_on_i is not None:
#         h_on_local = H_on_i - seg_start
#
#         search_start = h_on_local + 5
#         search_end = qrs_local - 2
#
#         sub = bp_segment[search_start:search_end]
#
#         if len(sub) > 5:
#             # find LAST zero-crossing before QRS
#             for i in range(len(sub) - 1, 1, -1):
#                 if sub[i - 1] > 0 and sub[i] < 0:
#                     H_off_i = seg_start + search_start + i
#                     break
#
#     # ---------- Intervals ----------
#     # Matched exactly to the Colab reference: if H_on_i was not found,
#     # AH_ms/HV_ms are NaN (genuinely "not measured"), NOT a forced
#     # substitute value. A previous revision of this file forced
#     # H_on_i = A_wave_i as a fallback, which made AH_ms silently read
#     # exactly 0.0 (a real-looking but fake number) while HV_ms silently
#     # absorbed the true AH duration - indistinguishable from a real
#     # zero-length AH interval. Colab's own table-building code reports
#     # NaN in this situation instead, which is the honest signal that
#     # detection failed for this beat rather than a false measurement.
#     PA_ms = A_wave_i - p_on_i
#     if H_on_i is not None:
#         AH_ms = H_on_i - A_wave_i
#         HV_ms = QRS_on_i - H_on_i
#     else:
#         AH_ms = float("nan")
#         HV_ms = float("nan")
#     PR_ms = QRS_on_i - p_on_i
#     QRS_ms = QRS_off_i - QRS_on_i
#
#     return {
#         "PA": float(PA_ms),
#         "AH": float(AH_ms),
#         "HV": float(HV_ms),
#         "PR": float(PR_ms),
#         "QRS": float(QRS_ms),
#         # SNR is intentionally NOT set here - compute_his_bundle_intervals
#         # overrides it with the whole-buffer SNR (matching Colab), since
#         # per-beat SNR was superseded by that whole-buffer measurement.
#         "SNR": None,
#         # Landmark sample indices for the beat that was actually used,
#         # so a caller can plot exactly what was measured instead of
#         # just the raw trace. Not part of the original 6-value result,
#         # purely additive - existing callers that only read
#         # PA/AH/HV/PR/QRS/SNR are unaffected. H_on_i/H_off_i may be
#         # None here if detection genuinely failed for this beat - the
#         # graph code must guard against that (see analyzelead4 below).
#         "R_peak_i": R_peak_i,
#         "p_on_i": p_on_i,
#         "A_wave_i": A_wave_i,
#         "H_on_i": H_on_i,
#         "H_off_i": H_off_i,
#         "QRS_on_i": QRS_on_i,
#         "QRS_off_i": QRS_off_i,
#         "seg_start": seg_start,
#         "seg_end": seg_end,
#         "bp_scaled": bp_scaled,
#     }
#
#
# def analyzelead4(samples, graph_path):
#     """
#     Analyze Lead4 ECG data and return ECG intervals.
#     Returns: [PA, AH, HV, PR, QRS, SNR, graph_path]
#     (HH has been removed — it is no longer computed or returned.)
#     This function is called from Android OfflineProcessor.
#     """
#     try:
#         import matplotlib
#         matplotlib.use("Agg")
#         import matplotlib.pyplot as plt
#
#         fs = 1000
#
#         # ----------------------------------
#         # Convert Java ArrayList to Python list
#         # ----------------------------------
#         try:
#             # Direct conversion from Java ArrayList to NumPy (faster)
#             size = samples.size()
#             lead4 = np.zeros(size, dtype=np.float64)
#             for i in range(size):
#                 lead4[i] = float(samples.get(i))
#         except Exception:
#             # Already Python list
#             lead4 = np.array([float(x) for x in samples], dtype=np.float64)
#
#         # Quick validation
#         if len(lead4) == 0:
#             return [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ""]
#
#         # ----------------------------------
#         # Convert Lead4 to V2 (mV)
#         # ----------------------------------
#         gain = 1
#         scale = 0.000286
#         v2 = ((lead4) * scale) * (-(gain))
#
#         # ----------------------------------
#         # Real interval detection (same algorithm process() uses)
#         # ----------------------------------
#         result = compute_his_bundle_intervals(v2, fs=fs)
#
#         # ----------------------------------
#         # Generate Graph
#         # ----------------------------------
#         # Two panels: (1) the full raw trace for context, and (2) a
#         # zoomed-in view of the actual beat that was measured, with the
#         # detected landmarks marked. The old graph only showed panel 1,
#         # which meant there was no way to visually confirm WHICH beat
#         # was used or WHERE the PA/AH/HV/PR/QRS boundaries were placed.
#         fig, axes = plt.subplots(2, 1, figsize=(12, 9))
#
#         axes[0].plot(v2, color='blue', linewidth=0.8)
#         axes[0].set_title('Full ECG Trace (Lead4 / V2)')
#         axes[0].set_xlabel('Sample')
#         axes[0].set_ylabel('Amplitude (mV)')
#         axes[0].grid(True)
#
#         margin = 150
#         zoom_start = max(0, result["seg_start"] - margin)
#         zoom_end = min(len(v2), result["QRS_off_i"] + margin)
#
#         if zoom_end > zoom_start:
#             x_zoom = np.arange(zoom_start, zoom_end)
#             axes[1].plot(x_zoom, v2[zoom_start:zoom_end], color='black', linewidth=1.0,
#                          label='Raw beat')
#
#             # Overlay the bandpassed His-Purkinje component (Colab's
#             # bp_scaled), aligned to its own P-onset -> QRS-onset x-range
#             # rather than the wider zoom window, and scaled for
#             # visibility against the much larger QRS/P amplitudes.
#             bp_scaled = result.get("bp_scaled")
#             if bp_scaled is not None and len(bp_scaled) > 0:
#                 x_bp = np.arange(result["seg_start"], result["seg_start"] + len(bp_scaled))
#                 axes[1].plot(x_bp, bp_scaled, color='teal', linewidth=1.2, linestyle='-',
#                              alpha=0.85, label='Bandpassed His signal (scaled)')
#
#             landmarks = [
#                 ('P-onset', result["p_on_i"], 'green'),
#                 ('A-wave', result["A_wave_i"], 'orange'),
#                 ('H-onset', result["H_on_i"], 'purple'),
#                 ('H-offset', result["H_off_i"], 'brown'),
#                 ('QRS-onset', result["QRS_on_i"], 'red'),
#                 ('QRS-offset', result["QRS_off_i"], 'red'),
#                 ('R-peak', result["R_peak_i"], 'blue'),
#             ]
#
#             y_top = np.max(v2[zoom_start:zoom_end])
#             for label, idx, color in landmarks:
#                 # idx can be None now if H-onset/H-offset detection
#                 # genuinely failed for this beat (AH/HV reported as NaN
#                 # to match Colab) - skip plotting that landmark instead
#                 # of crashing on "None < int".
#                 if idx is not None and zoom_start <= idx < zoom_end:
#                     axes[1].axvline(idx, color=color, linestyle='--', linewidth=1)
#                     axes[1].text(idx, y_top, label, rotation=90,
#                                  va='top', ha='right', fontsize=8, color=color)
#
#             axes[1].legend(loc='lower right', fontsize=8)
#
#         axes[1].set_title('Best His Bundle Segment (Detected Landmarks)')
#         axes[1].set_xlabel('Sample')
#         axes[1].set_ylabel('Amplitude (mV)')
#         axes[1].grid(True)
#
#         plt.tight_layout()
#         plt.savefig(graph_path, dpi=150, bbox_inches='tight')
#         plt.close()
#
#         # ----------------------------------
#         # Return: [PA, AH, HV, PR, QRS, SNR, graph_path]
#         # ----------------------------------
#         return [
#             result["PA"],
#             result["AH"],
#             result["HV"],
#             result["PR"],
#             result["QRS"],
#             result["SNR"],
#             str(graph_path)
#         ]
#
#     except Exception as e:
#         import traceback
#         print("================================")
#         print("PYTHON ERROR in analyzelead4")
#         print(str(e))
#         traceback.print_exc()
#         print("================================")
#         return [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ""]
#
#
# def process(buffer2):
#     """
#     Console/debug entry point. Uses the exact same real algorithm
#     as analyzelead4() via compute_his_bundle_intervals(), then
#     prints a formatted table.
#     """
#     fs = 1000
#
#     v2 = np.array(buffer2, dtype=np.float64)
#     gain = 1
#     scale = 0.000286
#     # Convert to mV and invert
#     v2 = ((v2) * scale) * (-(gain));
#
#     result = compute_his_bundle_intervals(v2, fs=fs)
#
#     PA_ms = result["PA"]
#     AH_ms = result["AH"]
#     HV_ms = result["HV"]
#     PR_ms = result["PR"]
#     QRS_ms = result["QRS"]
#     SNR_dB = result["SNR"]
#
#     ### Display #####
#     df = pd.DataFrame(
#         [[int(PA_ms), int(AH_ms), int(HV_ms), int(PR_ms), int(QRS_ms)]],
#         columns=[
#             "PA (ms)",
#             "AH (ms)",
#             "HV (ms)",
#             "PR (ms)",
#             "QRS (ms)"
#         ]
#     )
#
#     # ANSI escape codes
#     BOLD = "\033[1m"
#     BLACK = "\033[30m"
#     RESET = "\033[0m"
#     RED = "\033[91m"
#     BLUE = "\033[94m"
#     print("\n")
#     print(BOLD + BLACK + "=" * 60 + RESET)
#     print(BOLD + BLACK + "           BEST  HIS BUNDLE SEGMENT" + RESET)
#     print(BOLD + BLACK + "=" * 60 + RESET)
#     print()
#     print(BOLD + df.to_string(index=False) + RESET)
#     print(BOLD + BLACK + "=" * 60 + RESET)
#     print()
#
#     snr_color = RED if SNR_dB < 6 else BLUE
#     print(f"{BOLD}{snr_color}SNR of Best Segment : {SNR_dB:.2f} dB{RESET}")
#     print(BOLD + BLACK + "=" * 60 + RESET)
#
#     return {
#         "PA": PA_ms,
#         "AH": AH_ms,
#         "HV": HV_ms,
#         "PR": PR_ms,
#         "QRS": QRS_ms,
#         "SNR": SNR_dB
#     }





## ============================================================
## FIXED VERSION 2
## Changes in this revision:
##   1. HH value removed everywhere (no longer computed, no
##      longer returned to Android, no longer in the debug table).
##   2. THE MAIN BUG: analyzelead4() -- the function Android
##      actually calls -- was NOT using the real PA/AH/HV
##      detection algorithm. It was using placeholder guesses:
##          AH_ms = PR_ms * 0.3
##          HV_ms = QRS_ms * 0.2
##      These are not measurements, just arbitrary fractions of
##      PR/QRS, which is why the on-device numbers were wrong.
##      The *real* algorithm (bandpass filter -> A-wave peak ->
##      H-on/H-off zero-crossing detection) only existed inside
##      process(), which the app never called.
##   3. Fix: the real algorithm from process() has been pulled
##      out into one shared function,
##      compute_his_bundle_intervals(), and BOTH process() and
##      analyzelead4() now call it. So the values Android
##      receives are the same real measurement, not a guess.
##   4. No thresholds, window sizes, filter cutoffs, or any other
##      numeric parameter/logic used by the real algorithm were
##      changed -- only removed the fake approximations and wired
##      up the real one instead.
##   5. R_Peak_Detection_05: fixed IndexError crash.
##      "len(max_peak) < 2" matched BOTH zero peaks found AND one
##      peak found, then unconditionally indexed max_peak[0] --
##      which throws IndexError on an empty array. find_peaks()
##      requires a true local max with lower neighbors on both
##      sides, so a flat/noisy 140-sample window can legitimately
##      return zero peaks. Changed to "== 1" so only the true
##      single-peak case indexes max_peak[0]; zero-peak and
##      multi-peak cases both fall back to argmax(seg), same as
##      the pre-existing multi-peak fallback. Applied to both the
##      R-peak block and the mirrored S-wave block.
##   6. R_Peak_Detection_05: replaced the hardcoded threshold
##      window max(data_S[1000:5000]) with a whole-buffer robust
##      percentile (99.5th) so short buffers, or buffers where
##      the first ~4s happens to be noisy/quiet, don't miscalibrate
##      the peak-detection threshold for the entire buffer.
## ============================================================


# working code till 20/07/2026
#
# import math
# import collections
# import datetime
# import numpy as np
# import scipy as sp
# from scipy import signal
# from scipy.signal import butter, filtfilt
# import statistics as st
# import pandas as pd
# from pytz import timezone
#
# try:
#     import condat_tv
# except ImportError:
#     condat_tv = None
#     print("WARNING: condat_tv not available on this device — "
#           "sparse/TV denoise step will be skipped for this run.")
#
#
# ## data loss correction ##
#
# def data_loss(input_signal):
#     for x in range(1, len(input_signal) - 1, 1):  #### data loss
#         if (input_signal[
#             x] == 0):  ## checks zero values in input signal and replaces with the mean of adjacent non zero values
#             input_signal[x] = st.mean([input_signal[x - 1], input_signal[x + 1]])
#     return (input_signal)
#
#
# ## time stamp ##
# def get_time_from_timestamp(
#         timestamp):  ## converting unit timestamp to a readable date and time format
#     read_able = datetime.datetime.fromtimestamp(timestamp)
#     now_asia = str(read_able.astimezone(timezone('Asia/Kolkata')));  # print(now_asia)
#     year = now_asia[0:10]
#     year = year[8:10] + "-" + year[5:7] + "-" + year[0:4]
#     time = now_asia[11:19]
#     return (year, time)
#
#
# ## function extracts values from a dictionary given a specific index ##
# def Value_axis(data_dict, value):
#     data = list(data_dict.items())
#     an_array = np.array(data, dtype=object)
#     Values_1 = an_array[value]
#     Values_2 = np.asarray(Values_1[1])
#     return Values_2
#
#
# ## smoothening filter: Baseline filter+powerline noise+150 hz low pass filter ##
# def ecg_filters_V5_smooth(lead_data,
#                           hp=1.5):  ####  hp=0.67): According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     for x in range(1, len(lead_data), 1):
#         if (lead_data[x] == 0):
#             lead_data[x] = lead_data[x - 1]
#
#     ##### Baseline filter without ST Segment distortion
#     b = signal.firwin(2377, cutoff=[hp / 500], window="hamming", pass_zero=False);
#     a = 1
#     # b = signal.firwin(1735, cutoff = [0.67/500], window = "hamming", pass_zero=False); a = 1
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     # h, g = signal.butter(2, [45/500] , btype='low', analog=False)
#     h, g = signal.butter(2, [45 / 500], btype='low', analog=False)
#     j, i = signal.butter(2, [15 / 500], btype='low', analog=False)
#
#     filt_BW = signal.filtfilt(b, a, lead_data);
#     filt_SM = (signal.lfilter(j, i, filt_BW))
#     filt_LP = (signal.lfilter(h, g, filt_BW))
#
#     sparse = filt_LP - filt_SM;
#     if condat_tv is not None:
#         denoise = condat_tv.tv_denoise(sparse, 6.5)
#     else:
#         denoise = sparse  # fallback if the native lib isn't bundled on device
#     smooth_data = filt_SM + denoise
#
#     return smooth_data
#
#
# def baseline_filter(lead_data, hp=0.67):
#     ##### Baseline filter without ST Segment distortion
#     b = signal.firwin(2377, cutoff=[hp / 500], window="hamming", pass_zero=False);
#     a = 1
#     filt_BW = signal.filtfilt(b, a, lead_data);
#     return filt_BW
#
#
# def ecg_filters_V5_01(lead_data):
#     """
#     ECG Filtering according to IEC 60601-2-25
#     - Baseline wander removal
#     - 50 Hz notch
#     - 100 Hz notch
#     - 45 Hz low-pass
#     - Optional Savitzky-Golay smoothing
#
#     NOTE: replaces the previous hand-rolled powerline-notch coefficients
#     (applied via signal.lfilter) with signal.iirnotch + filtfilt. This
#     is functionally the same specification (50Hz/100Hz notch, 45Hz
#     low-pass, same ~1Hz baseline high-pass cutoff) but zero-phase
#     throughout, instead of the mix of filtfilt (baseline) + lfilter
#     (notch/low-pass) the previous version used. lfilter introduces a
#     real group delay; filtfilt does not. This also reduces the phase
#     offset this signal previously had relative to other pass-1
#     computations that assumed lead_data timing was preserved.
#     """
#
#     lead_data = np.asarray(lead_data, dtype=float).copy()
#
#     # Replace missing (zero) samples
#     for i in range(1, len(lead_data)):
#         if lead_data[i] == 0:
#             lead_data[i] = lead_data[i - 1]
#
#     Fs = 1000.0
#
#     # ------------------------------------------------------------------
#     # Baseline Wander Removal (High-pass FIR)
#     # ------------------------------------------------------------------
#     hp_coeff = signal.firwin(
#         numtaps=2377,
#         cutoff=1,
#         fs=Fs,
#         pass_zero=False,
#         window="hamming"
#     )
#
#     baseline_removed = signal.filtfilt(hp_coeff, [1.0], lead_data)
#
#     # ------------------------------------------------------------------
#     # 50 Hz Notch Filter
#     # ------------------------------------------------------------------
#     notch50_b, notch50_a = signal.iirnotch(
#         w0=50,
#         Q=30,
#         fs=Fs
#     )
#
#     filtered = signal.filtfilt(notch50_b, notch50_a, baseline_removed)
#
#     # ------------------------------------------------------------------
#     # 100 Hz Notch Filter
#     # ------------------------------------------------------------------
#     notch100_b, notch100_a = signal.iirnotch(
#         w0=100,
#         Q=30,
#         fs=Fs
#     )
#
#     filtered = signal.filtfilt(notch100_b, notch100_a, filtered)
#
#     # ------------------------------------------------------------------
#     # 45 Hz Low-pass Butterworth
#     # ------------------------------------------------------------------
#     lp_b, lp_a = signal.butter(
#         N=4,
#         Wn=45,
#         btype='low',
#         fs=Fs
#     )
#
#     filtered = signal.filtfilt(lp_b, lp_a, filtered)
#
#     # ------------------------------------------------------------------
#     # Optional smoothing
#     # ------------------------------------------------------------------
#     filtered = signal.savgol_filter(
#         filtered,
#         window_length=29,
#         polyorder=3
#     )
#
#     return filtered
#
#
# def phasor_transform(signal_in, Rv):
#     PT = np.empty_like(signal_in, dtype=float)
#     for i in range(len(signal_in)):
#         ch = signal_in[i] / Rv
#         PT[i] = math.degrees(math.atan(ch))
#         # PT[i] = Rv+ ()
#     return (PT)
#
#
# def find_PVC(signal_in, Rpeaks, Ramplitued, RRinterval):
#     ##   LIST OF AREA UNDER THE CURVE
#     AUC = collections.deque()
#     n_sig = len(signal_in)
#     for k in Rpeaks:
#         # Guard against negative slice start wrapping around to the END
#         # of the array (Python silently allows signal_in[-5:10], which
#         # silently corrupts the AUC/PVC score for early beats instead
#         # of raising an error).
#         lo = max(0, k - 80)
#         hi = min(n_sig, k + 80)
#         ch = abs(np.trapz(signal_in[lo:hi]));
#         AUC.append(ch)
#         # print("median : ",ch, 1.3*st.median(AUC))
#     AUC = np.array(AUC);  # print(len(AUC),AUC)
#     ##    ECTOPIC FUNCTION
#     PVC_list = np.empty_like(Rpeaks, dtype=float);
#     PVC_list[:] = np.nan
#     SVC_list = np.empty_like(Rpeaks, dtype=float);
#     SVC_list[:] = np.nan
#     for i in range(1, len(RRinterval)):
#         if RRinterval[i] >= (1.5 * RRinterval[i - 1]):
#             if Ramplitued[i] >= (Ramplitued[i - 1] + (0.2 * Ramplitued[i - 1])):  # PVC
#                 # Guard: at i == 1, AUC[0:i-1] == AUC[0:0] == empty,
#                 # and statistics.median() raises StatisticsError on an
#                 # empty sequence. There's no preceding-beat baseline to
#                 # compare against yet, so skip the PVC check for the
#                 # very first comparable beat instead of crashing the
#                 # whole buffer's analysis.
#                 if i > 1 and AUC[i] > (1.3 * st.median(AUC[0:i - 1])):
#                     PVC_list[i] = Rpeaks[i]
#             else:  # SVC or PVC
#                 if i > 1:
#                     if AUC[i] > (1.3 * st.median(AUC[0:i - 1])):
#                         PVC_list[i] = Rpeaks[i]
#     return np.array(PVC_list)
#
#
# def ecg_filters_Pwave(
#         lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     h, g = sp.signal.butter(4, [40 / 500], btype='low', analog=False);
#     j, i = signal.butter(2, [15 / 500], btype='low', analog=False)
#     filt_50 = sp.signal.lfilter(d, c, lead_data);
#     filt_100 = sp.signal.lfilter(f, e, filt_50)
#     filt_LP = sp.signal.lfilter(h, g, filt_50)
#     smoothed = sp.signal.savgol_filter(filt_LP, window_length=19, polyorder=3)
#     n = 41;
#     baseline_wander = sp.signal.medfilt(smoothed, kernel_size=n);
#     return baseline_wander
#
#
# def calculate_p_offset_p_duration_and_average_pr(p_loc, r_peak_ecg_signal, qrs_onset,
#                                                  window_size=60):
#     p_offset = []
#     p_duration = []
#     pr_differences = []
#
#     for n in range(len(p_loc)):
#         loc2 = p_loc[n] + window_size
#         p_start = r_peak_ecg_signal[loc2]
#         p_off = r_peak_ecg_signal[loc2 + 1]
#         count = 1
#
#         while p_start < p_off:
#             p_start = r_peak_ecg_signal[loc2 + count]
#             p_off = r_peak_ecg_signal[loc2 + 1 + count]
#             count = count + 1
#
#         p_offset.append(loc2 + count)
#         p_duration.append(qrs_onset[n] - (p_loc[n] - count))
#         pr_differences.append(qrs_onset[n] - (loc2 + count))
#
#     average_p_duration = np.mean(np.subtract(p_offset, p_loc))
#     average_pr = np.mean(pr_differences) + 80
#     if average_pr < 80:
#         average_pr = average_pr + 100
#     if average_pr < 0:
#         average_pr = average_pr + 150
#
#     return p_offset, p_duration, average_pr
#
#
# def detect_qrs_onset_offset(r_location, r_peak_ecg_signal, onset_window=50,
#                             offset_window=50):  ### onset offset change into 80 80 when bundle brunch block
#     qrs_onset = []
#     qrs_offset = []
#     signal_len = len(r_peak_ecg_signal)
#
#     for m in range(len(r_location)):
#         # QRS Onset
#         loc = r_location[m] - onset_window
#
#         if loc <= 0:
#             # Guard: negative loc would silently wrap around and index
#             # from the END of the array instead of erroring, corrupting
#             # this beat (and anything downstream, e.g. Mode_Value).
#             qrs_onset.append(0)
#         else:
#             R_peak = r_peak_ecg_signal[loc]
#             R_prev = r_peak_ecg_signal[loc - 1]
#             count = 1
#
#             while R_peak > R_prev and (loc - count - 1) >= 0:
#                 R_peak = r_peak_ecg_signal[loc - count]
#                 R_prev = r_peak_ecg_signal[loc - count - 1]
#                 count = count + 1
#
#             qrs_onset.append(max(0, loc - count))
#
#         # QRS Offset
#         loc2 = r_location[m] + offset_window
#
#         if loc2 >= signal_len - 1:
#             qrs_offset.append(signal_len - 1)
#         else:
#             R_on = r_peak_ecg_signal[loc2]
#             R_end = r_peak_ecg_signal[loc2 + 1]
#             count = 1
#
#             while R_on < R_end and (loc2 + count + 1) < signal_len:
#                 R_on = r_peak_ecg_signal[loc2 + count]
#                 R_end = r_peak_ecg_signal[loc2 + 1 + count]
#                 count = count + 1
#
#             qrs_offset.append(min(signal_len - 1, loc2 + count))
#
#     return qrs_onset, qrs_offset
#
#
# def calculate_pr_interval(r_location, qrs_onset, ecg_signal, offset_value=80):
#     r_loc_new = np.empty(len(r_location), dtype=int)
#     pr_array = np.empty(len(r_location))
#
#     for v in range(len(r_location)):
#         r_loc_new[v] = r_location[v] - offset_value
#         pr_array[v] = np.mean(ecg_signal[(r_loc_new[v]):(qrs_onset[v])])
#
#     return r_loc_new, pr_array
#
#
# def calculate_mode(pr_array):
#     mode_value = st.mode(pr_array)
#     return mode_value
#
#
# def calculate_new_qrs_onset_offset(r_location, r_peak_ecg_signal, mode_value, offset_before=50,
#                                    offset_after=50):  ###  offset before and offset after  make it 80 80 if bundle bunch block
#     new_qrs_onset = []
#     new_qrs_offset = []
#     signal_len = len(r_peak_ecg_signal)
#
#     for a in range(len(r_location)):
#         # QRS Onset
#         loc = r_location[a] - offset_before
#         if loc <= 0:
#             new_qrs_onset.append(0)
#         else:
#             R_peak = r_peak_ecg_signal[loc]
#             R_prev = r_peak_ecg_signal[loc - 1]
#             count = 1
#             while R_peak > R_prev > mode_value and (loc - count - 1) >= 0:  # Added boundary check
#                 R_peak = r_peak_ecg_signal[loc - count]
#                 R_prev = r_peak_ecg_signal[loc - count - 1]
#                 count = count + 1
#             new_qrs_onset.append(max(0, loc - count + 1))  # Adjusted for consistency
#
#     for b in range(len(r_location)):
#         # QRS Offset
#         loc2 = r_location[b] + offset_after
#         if loc2 >= signal_len - 1:  # If loc2 is already at or beyond the last valid index, the offset is the last index.
#             new_qrs_offset.append(signal_len - 1)
#         else:
#             # Check loc2 + 1 before accessing it in r_end
#             if loc2 + 1 >= signal_len:
#                 new_qrs_offset.append(
#                     signal_len - 1)  # If loc2 is valid but loc2+1 is not, offset is the last index
#             else:
#                 r_on = r_peak_ecg_signal[loc2]
#                 r_end = r_peak_ecg_signal[loc2 + 1]
#                 count = 1
#                 while r_on < r_end < mode_value and (
#                         loc2 + count + 1) < signal_len:  # Added boundary check
#                     r_on = r_peak_ecg_signal[loc2 + count]
#                     r_end = r_peak_ecg_signal[loc2 + 1 + count]
#                     count = count + 1
#                 new_qrs_offset.append(
#                     min(signal_len - 1, loc2 + count - 1))  # Adjusted for consistency
#
#     return new_qrs_onset, new_qrs_offset
#
#
# def R_Peak_Detection_05(raw_data):
#     peaks_up = [];
#     amp = [];
#     peaks_up_s = [];
#     ecp = [];
#     b = signal.firwin(1377, cutoff=[2 / 500], window="hamming", pass_zero=False);
#     a = 1
#     h, g = signal.butter(4, [45 / 500], btype='low', analog=False)
#     filt_BW = signal.filtfilt(b, a, raw_data);
#     data_F = signal.lfilter(h, g, filt_BW)
#     data_D = np.diff(data_F);
#     data_S = data_D * data_D
#     # Robust whole-buffer threshold instead of a hardcoded [1000:5000]
#     # window. The old fixed window assumed the first ~4 seconds of the
#     # buffer were always representative of QRS amplitude - if that
#     # window happened to contain noise/motion artifact (or was unusually
#     # quiet) while the rest of the buffer was clean, the threshold was
#     # miscalibrated for the ENTIRE buffer. A high percentile over the
#     # whole signal is robust to a few extreme edge-filter-transient
#     # samples while still scaling with genuine QRS amplitude, and works
#     # regardless of buffer length (1s, 15s, or otherwise).
#     robust_peak_ref = np.percentile(data_S, 99.5)
#     peaks, rpeak = signal.find_peaks(data_S, distance=255, height=(robust_peak_ref / 1.7));
#     n = len(peaks);
#
#     for i in range(0, n):
#         win = 70;
#         if (peaks[i] - (win)) < 0:
#             start_win = 0
#         else:
#             start_win = (peaks[i] - (win))
#         seg = data_F[start_win:(peaks[i]) + (
#             win)];  # seg = seg*seg; ############## Multiple Seg for getting the R or S peak
#         max_peak, peak_amp = signal.find_peaks(seg, distance=10, height=(max(seg) / 2))
#         # NOTE: "len(max_peak) < 2" also matches ZERO peaks found, and
#         # max_peak[0] then crashes with IndexError on an empty array.
#         # find_peaks() requires a true local max with lower neighbors on
#         # both sides, so a peak sitting right at the edge of this window
#         # (or a flat/noisy segment) can legitimately return zero peaks.
#         # Only trust max_peak[0] when exactly one peak was found; zero
#         # or multiple peaks both fall back to a plain argmax of the
#         # segment, same as the pre-existing multi-peak fallback below.
#         if len(max_peak) == 1:
#             peaks_u = start_win + max_peak[0]
#         else:
#             peaks_u = start_win + np.argmax(seg)
#         peaks_up.append(peaks_u);
#
#         seg_s = seg * (-1);
#         max_peak, peak_amp = signal.find_peaks(seg_s, distance=10)  # , height = (max(seg_s)/2))
#         if len(max_peak) == 1:
#             peaks_u = start_win + max_peak[0]  ########### need fix for short S wave
#         else:
#             peaks_u = start_win + np.argmax(seg_s)
#         peaks_up_s.append(peaks_u);
#
#     rr_int = np.diff(peaks_up);
#     th_ep = st.mean(rr_int) + st.mean(rr_int) / 10
#
#     for j in range(0, len(rr_int)):
#         if rr_int[j] > th_ep:
#             ep_seg = (data_F[peaks_up[j] + 60:peaks_up[j + 1] - 60])
#             max_e = np.argmax(ep_seg * ep_seg)  ########### for irregularity needs to be fixed
#             max_e = max_e + peaks_up[j] + 60;
#             # if ep_data[max_e] > (data_F[peaks_up[j]]/2):
#             ecp.append(max_e)
#     r_peak_values = [data_F[peak] for peak in peaks_up]
#     return peaks_up, ecp, peaks_up_s
#
#
# def P_Detection(rawECG, R_peaks, RRint, Ramp):
#     Filt_ECG = ecg_filters_V5_smooth(rawECG)
#     peaks = R_peaks;
#
#     PVC = find_PVC(Filt_ECG, peaks, Ramp, RRint)
#     PT_pwave = phasor_transform(ecg_filters_Pwave(rawECG), Rv=0.05)
#     P_st = [];
#     P_sp = [];
#     loc = [];
#     Pamp = []
#     P_location = [];
#     Pst = [];
#     Psp = []
#     for k in range(0, len(peaks)):
#         if k == 0:  # 1st P-peak detection
#             win_1 = len(Filt_ECG[0:peaks[0]]);  # print(win_1)
#             if win_1 > 300:
#                 win_st = peaks[0] - 300;
#                 win_sp = peaks[0] - 80
#                 ch = np.max(PT_pwave[peaks[0] - 300:peaks[0] - 80]);
#             elif win_1 <= 300 and win_1 >= 80:
#                 win_st = 0;
#                 win_sp = peaks[0] - 80
#                 ch = np.max(PT_pwave[0:peaks[0] - 80]);
#             elif win_1 < 80:
#                 P_location.append(np.nan);
#                 win_st = np.nan;
#                 win_sp = np.nan
#                 break;
#         elif k > 0 and k < len(peaks) - 1:  # Second to Last but 1 peak
#             win_st = int(peaks[k] - (0.4 * RRint[k]));
#             win_sp = int(peaks[k] - (0.05 * (RRint[k] - 100)));  # print(win_st,win_sp)
#             ch = np.max(PT_pwave[win_st:win_sp]);
#         else:  # Last peak
#             win_st = int(peaks[k] - (0.4 * RRint[k - 1]));
#             win_sp = int(peaks[k] - (0.05 * (RRint[k - 1] - 100)));  # print(win_st,win_sp)
#             ch = np.max(PT_pwave[win_st:win_sp]);
#         loc_ch = (np.where(PT_pwave == ch));
#         loc_ch = loc_ch[0];
#         if loc_ch[0] < 80:
#             loc_st = loc_ch[0]
#         else:
#             loc_st = loc_ch[0] - 80
#         ix = np.max(Filt_ECG[loc_st:loc_ch[0] + 80]);
#         loc_ch = (np.where(Filt_ECG == ix));
#         loc_ch = loc_ch[0];
#         loc.append(loc_ch[0]);
#         Pamp.append(np.abs(Filt_ECG[loc_ch[0]]))
#         P_st.append(win_st);
#         P_sp.append(win_sp)
#
#     # print("Pamp temp :", len(Pamp), Pamp);  print("Ploc temp :", len(loc), loc)
#
#     for m in range(0, len(RRint)):
#         # print(m)
#         if np.isnan(PVC[m]) == True and Pamp[m] > (
#                 0.5 * Filt_ECG[peaks[m]]):  # Beat Not a PVC: retain P loc
#             P_location.append(loc[m])
#             Pst.append(P_st[m]);
#             Psp.append(P_sp[m])
#
#     P_location = loc
#     P_onset = [];  # print("P location : ",P_location)
#     # print("Pst :", Pst); print("Psp :",Psp)
#     for m in range(len(P_location)):
#         loc = P_location[m]
#         T_on = Filt_ECG[loc - 0];
#         prev = Filt_ECG[loc - 20 - 1];
#         Count = 1
#         while T_on > prev:
#             T_on = Filt_ECG[loc - 0 - Count];
#             prev = Filt_ECG[loc - 20 - Count - 1];
#             Count = Count + 1
#         P_onset.append(loc - Count);
#     return (P_location, Pamp, P_onset)
#
#
# def ecg_filters_Twave(
#         lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     h, g = sp.signal.butter(4, [20 / 500], btype='low', analog=False)
#     j, i = sp.signal.butter(2, [15 / 500], btype='low', analog=False)
#     filt_50 = sp.signal.lfilter(d, c, lead_data);  # filt_100 = sp.signal.lfilter(f, e, filt_50)
#     filt_LP = sp.signal.lfilter(h, g, filt_50)
#     smoothed = sp.signal.savgol_filter(filt_LP, window_length=19, polyorder=3)
#     n = 41;
#     baseline_wander = sp.signal.medfilt(smoothed, kernel_size=n);
#     return baseline_wander
#
#
# def T_Detection(raw_signal, R_peaks, RRint):
#     Filt_ECG = ecg_filters_V5_smooth(raw_signal)
#     T_filt = ecg_filters_Twave(raw_signal)
#     PT_twave = phasor_transform(T_filt, Rv=0.1)
#     T_st = [];
#     T_sp = [];
#     T_loc = [];
#     Tamp = []
#     for k in range(0, len(R_peaks)):
#         if k == len(R_peaks) - 1:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k - 1]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k - 1])));  # print(win_st,win_sp)
#         elif k > 0 and k < len(R_peaks) - 1:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k])));  # print(win_st,win_sp)
#         else:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k + 1]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k + 1])));  # print(win_st,win_sp)
#         # Check if the slice is empty and handle it.
#         # win_st < win_sp as raw integers does NOT guarantee a non-empty
#         # slice: if both fall past the end of the buffer (common for the
#         # last beat, whose window is sized off RRint[k-1]), Python
#         # silently truncates PT_twave[win_st:win_sp] to an empty array
#         # instead of raising - and np.max() on an empty array throws
#         # "zero-size array to reduction operation maximum which has no
#         # identity", which crashed the ENTIRE buffer's analysis (the
#         # exception propagates out of T_Detection -> compute_his_bundle_
#         # intervals -> analyzelead4's except block -> all-zero result).
#         # Clamp both bounds to the signal length first so the emptiness
#         # check reflects the slice that will actually be taken.
#         sig_len = len(PT_twave)
#         win_st_c = max(0, min(win_st, sig_len))
#         win_sp_c = max(0, min(win_sp, sig_len))
#         if win_st_c >= win_sp_c:
#             print(f"Warning: Empty slice for R-peak index {k}. win_st: {win_st}, win_sp: {win_sp}")
#             # Skip this iteration if the slice is empty
#             continue
#         else:
#             ch = np.max(PT_twave[win_st_c:win_sp_c])
#         loc_ch = (np.where(PT_twave == ch));
#         loc_ch = loc_ch[0];
#         # print("window :",loc_ch[0]-80,loc_ch[0]+80)
#
#         # Check if loc_ch is empty before indexing
#         if len(loc_ch) > 0:
#             # Clamp the +-80 window to valid bounds. loc_ch[0] comes from
#             # np.where(PT_twave == ch), which searches the WHOLE array
#             # for that value - not just the current beat's window - so
#             # it can legitimately land close to sample 0 for some beats.
#             # When it does, "loc_ch[0] - 80" goes negative and Python
#             # silently wraps that into a slice start near the END of the
#             # array (while the end of the slice is still small), making
#             # Filt_ECG[start:end] empty and crashing np.max() with
#             # "zero-size array to reduction operation maximum which has
#             # no identity" - the exact traceback this fixes.
#             ecg_len = len(Filt_ECG)
#             lo = max(0, loc_ch[0] - 80)
#             hi = min(ecg_len, loc_ch[0] + 80)
#             if hi <= lo:
#                 print(f"Warning: Empty ECG window for R-peak index {k} "
#                       f"(loc_ch[0]={loc_ch[0]}, buffer length {ecg_len}). Skipping.")
#                 continue
#             ix = np.max(Filt_ECG[lo:hi]);
#             loc_ch = (np.where(Filt_ECG == ix));
#             loc_ch = loc_ch[0];
#             if len(loc_ch) == 0:
#                 print(f"Warning: no matching Filt_ECG index for R-peak index {k}. Skipping.")
#                 continue
#             T_loc.append(loc_ch[0]);
#             Tamp.append(np.abs(Filt_ECG[loc_ch[0]]))
#             T_st.append(win_st);
#             T_sp.append(win_sp)
#         else:
#             print(f"Warning: loc_ch is empty for R-peak index {k}. Skipping this iteration.")
#             # Handle the case where loc_ch is empty, e.g., append NaN to T_loc, etc.
#
#     return (np.array(T_loc))
#
#
# def calculate_t_offset(t_loc, r_peak_ecg_signal):
#     t_offset = []
#     signal_length = len(r_peak_ecg_signal)
#
#     for o in range(len(t_loc)):
#         location = t_loc[o]
#         t_start = r_peak_ecg_signal[location]
#         t_off = r_peak_ecg_signal[min(location + 1, signal_length - 1)]
#         count = 1
#
#         while t_start < t_off and location + count + 1 < signal_length:
#             t_start = r_peak_ecg_signal[min(location + count, signal_length - 1)]
#             t_off = r_peak_ecg_signal[min(location + count + 1, signal_length - 1)]
#             count += 1
#
#         # Ensure the offset doesn't go out of bounds
#         final_offset = min(location + count + 100, signal_length - 1)
#         t_offset.append(final_offset)
#
#     return t_offset
#
#
# def calculate_snr_db(raw_signal, filtered_signal):
#     """
#     Whole-buffer SNR: compares the raw (unfiltered) signal against the
#     smoothed/filtered signal, treating the filtered signal as "signal"
#     and whatever the filter removed (raw - filtered) as "noise".
#
#     NOTE: this is an inferred standard implementation. The actual
#     calculate_snr_db() from the Colab reference notebook was not
#     provided - if it computes SNR differently, swap this out with the
#     real definition.
#     """
#     signal_power = np.mean(filtered_signal ** 2)
#     noise = raw_signal - filtered_signal
#     noise_power = np.mean(noise ** 2)
#
#     eps = 1e-12
#     if noise_power > eps:
#         return 10 * np.log10(max(signal_power, eps) / noise_power)
#     else:
#         return float("inf")
#
#
# def bandpass(signal_in, fs, lowcut=30, highcut=100, order=4):
#     # Matched exactly to the Colab reference implementation
#     # (def bandpass(signal, fs, lowcut=30, highcut=100, order=4)).
#     # A previous revision of this file used highcut=499.9, reasoning
#     # that a narrower 30-100Hz band was distorting the A-wave/H-onset
#     # deflections - that reasoning was never validated against the
#     # actual Colab reference and contradicted it, so it has been
#     # reverted back to the validated 30-100Hz band.
#     nyq = 0.5 * fs
#     b, a = butter(order, [lowcut / nyq, highcut / nyq], btype='band')
#     return filtfilt(b, a, signal_in)
#
#
# # ================================================================
# #   SHARED REAL ALGORITHM
# #   This is the single source of truth for PA / AH / HV / PR /
# #   QRS / SNR. Both process() (console/debug) and analyzelead4()
# #   (called from Android) call this so they can never drift out
# #   of sync again.
# # ================================================================
#
# def compute_his_bundle_intervals(v2, fs=1000, lsb_V=286e-9):
#     """
#     v2: already-scaled, already-inverted lead signal in mV, i.e.
#         v2 = (raw_samples * 0.000286) * (-gain)
#     Returns a dict with PA, AH, HV, PR, QRS (all ms) and SNR (dB).
#     Raises ValueError if a clean best-beat/segment could not be
#     resolved from this buffer (e.g. buffer too short / too noisy).
#     """
#
#     ###--------------------------- Raw_data fetching and R peak detection ----------------------------###
#     R_data = v2 * (-1)
#     R_peak_ecg_signal = ecg_filters_V5_01(R_data)
#     R_Location, ectopic, S_Location = R_Peak_Detection_05(R_data)
#     R_Location = np.array(R_Location, dtype=int)
#
#     # Keep this PASS-1 signal and R-peak positions under their own names.
#     # QRS_Onset/New_QRS_Onset/P_onset below are all computed against
#     # THIS signal/these positions. A second R-peak detection pass runs
#     # later (on a differently-filtered signal, for T-wave localization)
#     # and used to silently overwrite R_peak_ecg_signal/R_Location here -
#     # but ecg_filters_V5_01 and ecg_filters_V5_smooth use lfilter
#     # internally (not filtfilt), which introduces a real phase/group
#     # delay. In testing this produced a consistent ~6-sample offset
#     # between the "same" R-peak across the two passes. Measuring a beat
#     # (_evaluate_candidate_beat) by slicing the SECOND pass's signal
#     # using window boundaries computed from the FIRST pass's signal
#     # silently shifts the P-onset->QRS-onset segment by that offset -
#     # small in absolute terms, but large relative to the ~55-95 sample
#     # H-onset search window, and a plausible dominant cause of
#     # intermittently wrong/zero AH (and possibly HV) values with no
#     # error ever being raised. Fix: always measure a beat using the
#     # SAME signal/positions its P-onset and QRS-onset were derived from.
#     R_peak_ecg_signal_meas = R_peak_ecg_signal
#     R_Location_meas = R_Location
#
#     if len(R_Location) < 3:
#         raise ValueError("Not enough R peaks detected in this buffer.")
#
#     ###--------------------------Heart Rate calculation---------------------------------- ###
#     R_amp = [];
#     RRint = [];
#     for i in range(0, len(R_Location) - 1):
#         ch = R_Location[i + 1] - R_Location[i];
#         RRint.append(ch)
#         R_amp.append(R_peak_ecg_signal[R_Location[i]])
#         if i == (len(R_Location) - 2):
#             R_amp.append(R_peak_ecg_signal[R_Location[i + 1]])
#
#     QRS_Onset, QRS_Offset = detect_qrs_onset_offset(R_Location, R_peak_ecg_signal)
#     R_Loc_New, PR_Array = calculate_pr_interval(R_Location, QRS_Onset, R_peak_ecg_signal)
#     Mode_Value = calculate_mode(PR_Array)
#     New_QRS_Onset, New_QRS_Offset = calculate_new_qrs_onset_offset(R_Location, R_peak_ecg_signal,
#                                                                    Mode_Value)
#     P_location, Pamp, P_onset = P_Detection(R_data, R_Location, RRint, R_amp)
#
#     filtered_v2 = ecg_filters_V5_smooth(v2)
#     R_data = filtered_v2 * (-1)
#
#     # ---------- Whole-buffer SNR ----------
#     # Matched to the Colab reference: compares the ORIGINAL raw signal
#     # against the SMOOTHED signal across the entire buffer, not just
#     # the one selected beat's tiny His-bundle window. This measures
#     # overall recording quality rather than one beat's local quality.
#     raw_signal = v2 * (-1)
#     filtered_signal = R_data
#     SNR_dB = calculate_snr_db(raw_signal, filtered_signal)
#
#     R_peak_ecg_signal = ecg_filters_V5_smooth(R_data)
#     R_Location, ectopic, S_Location = R_Peak_Detection_05(R_data)
#     T_loc = T_Detection(R_peak_ecg_signal, R_Location, RRint)
#     T_Offset = calculate_t_offset(T_loc, R_peak_ecg_signal)
#
#     T_Offset = np.array(T_Offset, dtype=int)
#     P_location = np.array(P_location, dtype=int)
#     P_onset = np.array(P_onset, dtype=int)
#
#     ads_values_tloc_p_location = []
#     for i, t_offset in enumerate(T_Offset):
#         next_p_onset = next((p for p in P_onset if p > t_offset), None)
#         if next_p_onset is not None and next_p_onset < len(R_peak_ecg_signal):
#             seg_start = int(t_offset)
#             seg_end = int(next_p_onset)
#             ads_segment = np.round(np.abs(R_peak_ecg_signal[seg_start:seg_end] / 0.000286)).astype(
#                 int)
#             ads_values_tloc_p_location.append(ads_segment)
#
#     if not ads_values_tloc_p_location:
#         raise ValueError("No T→P segments found for ADS analysis.")
#
#     # -------------------------------------------------------------------------
#     #                SELECT BEST 5 SEGMENTS (by RMS voltage)
#     # -------------------------------------------------------------------------
#     segment_scores = []
#     segment_indices = []
#
#     for i, ads_segment in enumerate(ads_values_tloc_p_location):
#         ads_segment = np.asarray(ads_segment, dtype=float)
#         if len(ads_segment) < 10:  # ignore very small segments
#             continue
#         voltage = ads_segment * lsb_V
#         rms = np.sqrt(np.mean(voltage ** 2))
#         segment_scores.append(rms)
#         segment_indices.append(i)
#
#     segment_scores = np.array(segment_scores)
#     segment_indices = np.array(segment_indices)
#
#     if len(segment_indices) == 0:
#         raise ValueError("No valid segments (all shorter than 10 samples) for ADS analysis.")
#
#     # Matched to the validated Colab reference implementation: sort
#     # DESCENDING and take the highest-RMS segments first.
#     #
#     # NOTE: an earlier revision of this file inverted this to
#     # ascending/lowest-RMS, reasoning that a flat T->P baseline should
#     # indicate a cleaner beat. That reasoning turned out to be wrong -
#     # the user's Colab notebook (validated against known-correct
#     # values) uses this original descending/highest-RMS-first order,
#     # so it has been reverted to match.
#     sorted_order = np.argsort(segment_scores)[::-1]  # descending: highest RMS first
#     candidate_indices = segment_indices[sorted_order]  # ALL candidates, best (highest RMS) first
#
#     # Try every candidate beat, best (quietest baseline) first, and use
#     # the first one that actually passes validation. The old code only
#     # ever tried the single top-ranked candidate and raised/crashed the
#     # ENTIRE buffer's analysis if that one beat happened to fail a
#     # validity check (e.g. "P-onset is not before QRS-onset") - even
#     # though 4+ other perfectly usable candidate beats had already been
#     # computed and were sitting right there unused.
#     last_error = None
#     result_core = None
#
#     for candidate_idx in candidate_indices:
#         try:
#             result_core = _evaluate_candidate_beat(
#                 candidate_idx,
#                 P_onset=P_onset,
#                 R_Location=R_Location_meas,
#                 New_QRS_Onset=New_QRS_Onset,
#                 New_QRS_Offset=New_QRS_Offset,
#                 R_peak_ecg_signal=R_peak_ecg_signal_meas,
#                 fs=fs,
#             )
#             break
#         except ValueError as e:
#             last_error = e
#             continue
#
#     if result_core is None:
#         raise ValueError(
#             f"No usable beat found among {len(candidate_indices)} candidates "
#             f"(last error: {last_error})."
#         )
#
#     # Apply the whole-buffer SNR (matched to Colab) computed earlier,
#     # rather than a per-beat value - _evaluate_candidate_beat leaves
#     # "SNR" as None on purpose since this is the single source of truth.
#     result_core["SNR"] = float(SNR_dB)
#
#     return result_core
#
#
# def _evaluate_candidate_beat(
#         candidate_idx,
#         P_onset,
#         R_Location,
#         New_QRS_Onset,
#         New_QRS_Offset,
#         R_peak_ecg_signal,
#         fs,
# ):
#     """
#     Attempt to compute PA/AH/HV/PR/QRS/SNR for ONE candidate beat.
#     Raises ValueError if this particular beat doesn't pass validation
#     (caller is expected to try the next-best candidate in that case).
#     """
#
#     if candidate_idx >= len(P_onset) or candidate_idx >= len(R_Location):
#         raise ValueError("Candidate segment index out of range for P_onset / R_Location.")
#
#     R_peak_i = int(R_Location[candidate_idx])
#
#     # ---------- QRS onset ----------
#     qrs_candidates = [q for q in New_QRS_Onset if q < R_peak_i]
#     if len(qrs_candidates) == 0:
#         raise ValueError("No QRS onset found before this candidate's R peak.")
#     QRS_on_i = qrs_candidates[-1]
#
#     # ---------- QRS offset ----------
#     qrs_off_candidates = [q for q in New_QRS_Offset if q > R_peak_i]
#     if len(qrs_off_candidates) == 0:
#         raise ValueError("No QRS offset found after this candidate's R peak.")
#     QRS_off_i = qrs_off_candidates[0]
#
#     # ---------- P onset for this beat ----------
#     p_on_i = int(P_onset[candidate_idx])
#     if p_on_i >= QRS_on_i:
#         raise ValueError("P-onset is not before QRS-onset for this candidate beat.")
#
#     # ---------- Bandpass the P-onset -> QRS-onset segment ----------
#     seg_start = p_on_i
#     seg_end = QRS_on_i + 1
#     raw_segment = R_peak_ecg_signal[seg_start:seg_end]
#
#     if len(raw_segment) <= 10:
#         raise ValueError("P-onset to QRS-onset segment too short to bandpass filter.")
#
#     bp = bandpass(raw_segment, fs)
#
#     # ---------- Scaled bandpassed trace for graphing ----------
#     # Matches Colab's bp_scaled: normalize the bandpassed (His-Purkinje)
#     # component to +-1 and rescale it to roughly the same visual
#     # amplitude as the raw segment, purely so it can be overlaid on the
#     # raw trace and stay visible instead of looking flat next to the
#     # much larger QRS/P-wave amplitudes.
#     bp_abs_max = np.max(np.abs(bp))
#     if bp_abs_max > 0:
#         bp_norm = bp / bp_abs_max
#         scale = 0.5 * np.max(np.abs(raw_segment))
#         bp_scaled = bp_norm * scale
#     else:
#         bp_scaled = bp
#
#     # ---------- A wave (true peak in early PR region) ----------
#     qrs_local = QRS_on_i - seg_start
#     search_end = int(0.45 * qrs_local)
#     sub = bp[15:search_end]
#     if len(sub) == 0:
#         raise ValueError("A-wave search window is empty for this candidate beat.")
#     peak_idx = np.argmax(sub)
#     A_wave_i = seg_start + 15 + peak_idx
#
#     # -------------------------------------------------------
#     #        H_on and H_off DETECTION (TRUE BIPHASIC START)
#     # -------------------------------------------------------
#     H_on_i = None
#     bp_segment = bp  # bandpassed signal
#
#     # search region before QRS (where the circled wave is)
#     search_start = max(0, qrs_local - 60)
#     search_end = qrs_local - 5
#     sub = bp_segment[search_start:search_end]
#
#     # Step 1: find zero crossing (center of biphasic)
#     zc_idx = None
#     for i in range(1, len(sub)):
#         if sub[i - 1] < 0 and sub[i] > 0:
#             zc_idx = i
#             break
#
#     # Step 2: go BACK to find beginning of that wave.
#     # Matched exactly to the Colab reference's fixed 0.001 flatness
#     # threshold. A previous revision of this file made this adaptive
#     # (scaled to each beat's own amplitude) to reduce how often H_on_i
#     # went undetected - but that deviated from the validated Colab
#     # implementation without device data to justify it. Reverted to
#     # match Colab exactly; see the fallback behavior below for the
#     # correct way Colab itself handles a genuine non-detection.
#     if zc_idx is not None:
#         for i in range(zc_idx, 1, -1):
#             # beginning = where slope starts increasing
#             if abs(sub[i] - sub[i - 1]) < 0.001:
#                 H_on_i = seg_start + search_start + i
#                 break
#
#     # search between H_on and QRS
#     H_off_i = None
#     if H_on_i is not None:
#         h_on_local = H_on_i - seg_start
#
#         search_start = h_on_local + 5
#         search_end = qrs_local - 2
#
#         sub = bp_segment[search_start:search_end]
#
#         if len(sub) > 5:
#             # find LAST zero-crossing before QRS
#             for i in range(len(sub) - 1, 1, -1):
#                 if sub[i - 1] > 0 and sub[i] < 0:
#                     H_off_i = seg_start + search_start + i
#                     break
#
#     # ---------- Intervals ----------
#     # Matched exactly to the Colab reference: if H_on_i was not found,
#     # AH_ms/HV_ms are NaN (genuinely "not measured"), NOT a forced
#     # substitute value. A previous revision of this file forced
#     # H_on_i = A_wave_i as a fallback, which made AH_ms silently read
#     # exactly 0.0 (a real-looking but fake number) while HV_ms silently
#     # absorbed the true AH duration - indistinguishable from a real
#     # zero-length AH interval. Colab's own table-building code reports
#     # NaN in this situation instead, which is the honest signal that
#     # detection failed for this beat rather than a false measurement.
#     PA_ms = A_wave_i - p_on_i
#     if H_on_i is not None:
#         AH_ms = H_on_i - A_wave_i
#         HV_ms = QRS_on_i - H_on_i
#     else:
#         AH_ms = float("nan")
#         HV_ms = float("nan")
#     PR_ms = QRS_on_i - p_on_i
#     QRS_ms = QRS_off_i - QRS_on_i
#
#     return {
#         "PA": float(PA_ms),
#         "AH": float(AH_ms),
#         "HV": float(HV_ms),
#         "PR": float(PR_ms),
#         "QRS": float(QRS_ms),
#         # SNR is intentionally NOT set here - compute_his_bundle_intervals
#         # overrides it with the whole-buffer SNR (matching Colab), since
#         # per-beat SNR was superseded by that whole-buffer measurement.
#         "SNR": None,
#         # Landmark sample indices for the beat that was actually used,
#         # so a caller can plot exactly what was measured instead of
#         # just the raw trace. Not part of the original 6-value result,
#         # purely additive - existing callers that only read
#         # PA/AH/HV/PR/QRS/SNR are unaffected. H_on_i/H_off_i may be
#         # None here if detection genuinely failed for this beat - the
#         # graph code must guard against that (see analyzelead4 below).
#         "R_peak_i": R_peak_i,
#         "p_on_i": p_on_i,
#         "A_wave_i": A_wave_i,
#         "H_on_i": H_on_i,
#         "H_off_i": H_off_i,
#         "QRS_on_i": QRS_on_i,
#         "QRS_off_i": QRS_off_i,
#         "seg_start": seg_start,
#         "seg_end": seg_end,
#         "bp_scaled": bp_scaled,
#     }
#
#
# def analyzelead4(samples, graph_path):
#     """
#     Analyze Lead4 ECG data and return ECG intervals.
#     Returns: [PA, AH, HV, PR, QRS, SNR, graph_path]
#     (HH has been removed — it is no longer computed or returned.)
#     This function is called from Android OfflineProcessor.
#     """
#     try:
#         import matplotlib
#         matplotlib.use("Agg")
#         import matplotlib.pyplot as plt
#
#         fs = 1000
#
#         # ----------------------------------
#         # Convert Java ArrayList to Python list
#         # ----------------------------------
#         try:
#             # Direct conversion from Java ArrayList to NumPy (faster)
#             size = samples.size()
#             lead4 = np.zeros(size, dtype=np.float64)
#             for i in range(size):
#                 lead4[i] = float(samples.get(i))
#         except Exception:
#             # Already Python list
#             lead4 = np.array([float(x) for x in samples], dtype=np.float64)
#
#         # Quick validation
#         if len(lead4) == 0:
#             return [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ""]
#
#         # ----------------------------------
#         # Convert Lead4 to V2 (mV)
#         # ----------------------------------
#         gain = 1
#         scale = 0.000286
#         v2 = ((lead4) * scale) * (-(gain))
#
#         # ----------------------------------
#         # Real interval detection (same algorithm process() uses)
#         # ----------------------------------
#         result = compute_his_bundle_intervals(v2, fs=fs)
#
#         # ----------------------------------
#         # Generate Graph
#         # ----------------------------------
#         # Two panels: (1) the full raw trace for context, and (2) a
#         # zoomed-in view of the actual beat that was measured, with the
#         # detected landmarks marked. The old graph only showed panel 1,
#         # which meant there was no way to visually confirm WHICH beat
#         # was used or WHERE the PA/AH/HV/PR/QRS boundaries were placed.
#         fig, axes = plt.subplots(2, 1, figsize=(12, 9))
#
#         axes[0].plot(v2, color='blue', linewidth=0.8)
#         axes[0].set_title('Full ECG Trace (Lead4 / V2)')
#         axes[0].set_xlabel('Sample')
#         axes[0].set_ylabel('Amplitude (mV)')
#         axes[0].grid(True)
#
#         margin = 150
#         zoom_start = max(0, result["seg_start"] - margin)
#         zoom_end = min(len(v2), result["QRS_off_i"] + margin)
#
#         if zoom_end > zoom_start:
#             x_zoom = np.arange(zoom_start, zoom_end)
#             axes[1].plot(x_zoom, v2[zoom_start:zoom_end], color='black', linewidth=1.0,
#                          label='Raw beat')
#
#             # Overlay the bandpassed His-Purkinje component (Colab's
#             # bp_scaled), aligned to its own P-onset -> QRS-onset x-range
#             # rather than the wider zoom window, and scaled for
#             # visibility against the much larger QRS/P amplitudes.
#             bp_scaled = result.get("bp_scaled")
#             if bp_scaled is not None and len(bp_scaled) > 0:
#                 x_bp = np.arange(result["seg_start"], result["seg_start"] + len(bp_scaled))
#                 axes[1].plot(x_bp, bp_scaled, color='teal', linewidth=1.2, linestyle='-',
#                              alpha=0.85, label='Bandpassed His signal (scaled)')
#
#             landmarks = [
#                 ('P-onset', result["p_on_i"], 'green'),
#                 ('A-wave', result["A_wave_i"], 'orange'),
#                 ('H-onset', result["H_on_i"], 'purple'),
#                 ('H-offset', result["H_off_i"], 'brown'),
#                 ('QRS-onset', result["QRS_on_i"], 'red'),
#                 ('QRS-offset', result["QRS_off_i"], 'red'),
#                 ('R-peak', result["R_peak_i"], 'blue'),
#             ]
#
#             y_top = np.max(v2[zoom_start:zoom_end])
#             for label, idx, color in landmarks:
#                 # idx can be None now if H-onset/H-offset detection
#                 # genuinely failed for this beat (AH/HV reported as NaN
#                 # to match Colab) - skip plotting that landmark instead
#                 # of crashing on "None < int".
#                 if idx is not None and zoom_start <= idx < zoom_end:
#                     axes[1].axvline(idx, color=color, linestyle='--', linewidth=1)
#                     axes[1].text(idx, y_top, label, rotation=90,
#                                  va='top', ha='right', fontsize=8, color=color)
#
#             axes[1].legend(loc='lower right', fontsize=8)
#
#         axes[1].set_title('Best His Bundle Segment (Detected Landmarks)')
#         axes[1].set_xlabel('Sample')
#         axes[1].set_ylabel('Amplitude (mV)')
#         axes[1].grid(True)
#
#         plt.tight_layout()
#         plt.savefig(graph_path, dpi=150, bbox_inches='tight')
#         plt.close()
#
#         # ----------------------------------
#         # Return: [PA, AH, HV, PR, QRS, SNR, graph_path]
#         # ----------------------------------
#         return [
#             result["PA"],
#             result["AH"],
#             result["HV"],
#             result["PR"],
#             result["QRS"],
#             result["SNR"],
#             str(graph_path)
#         ]
#
#     except Exception as e:
#         import traceback
#         print("================================")
#         print("PYTHON ERROR in analyzelead4")
#         print(str(e))
#         traceback.print_exc()
#         print("================================")
#         return [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ""]
#
#
# def process(buffer2):
#     """
#     Console/debug entry point. Uses the exact same real algorithm
#     as analyzelead4() via compute_his_bundle_intervals(), then
#     prints a formatted table.
#     """
#     fs = 1000
#
#     v2 = np.array(buffer2, dtype=np.float64)
#     gain = 1
#     scale = 0.000286
#     # Convert to mV and invert
#     v2 = ((v2) * scale) * (-(gain));
#
#     result = compute_his_bundle_intervals(v2, fs=fs)
#
#     PA_ms = result["PA"]
#     AH_ms = result["AH"]
#     HV_ms = result["HV"]
#     PR_ms = result["PR"]
#     QRS_ms = result["QRS"]
#     SNR_dB = result["SNR"]
#
#     ### Display #####
#     df = pd.DataFrame(
#         [[int(PA_ms), int(AH_ms), int(HV_ms), int(PR_ms), int(QRS_ms)]],
#         columns=[
#             "PA (ms)",
#             "AH (ms)",
#             "HV (ms)",
#             "PR (ms)",
#             "QRS (ms)"
#         ]
#     )
#
#     # ANSI escape codes
#     BOLD = "\033[1m"
#     BLACK = "\033[30m"
#     RESET = "\033[0m"
#     RED = "\033[91m"
#     BLUE = "\033[94m"
#     print("\n")
#     print(BOLD + BLACK + "=" * 60 + RESET)
#     print(BOLD + BLACK + "           BEST  HIS BUNDLE SEGMENT" + RESET)
#     print(BOLD + BLACK + "=" * 60 + RESET)
#     print()
#     print(BOLD + df.to_string(index=False) + RESET)
#     print(BOLD + BLACK + "=" * 60 + RESET)
#     print()
#
#     snr_color = RED if SNR_dB < 6 else BLUE
#     print(f"{BOLD}{snr_color}SNR of Best Segment : {SNR_dB:.2f} dB{RESET}")
#     print(BOLD + BLACK + "=" * 60 + RESET)
#
#     return {
#         "PA": PA_ms,
#         "AH": AH_ms,
#         "HV": HV_ms,
#         "PR": PR_ms,
#         "QRS": QRS_ms,
#         "SNR": SNR_dB
#     }
#




#new code  09:58 20-07-2026
#
# import math
# import collections
# import datetime
# import numpy as np
# import scipy as sp
# from scipy import signal
# from scipy.signal import butter, filtfilt
# import statistics as st
# import pandas as pd
# from pytz import timezone
#
#
#
#
# ## data loss correction ##
#
# def data_loss(input_signal):
#     for x in range(1, len(input_signal) - 1, 1):  #### data loss
#         if (input_signal[
#             x] == 0):  ## checks zero values in input signal and replaces with the mean of adjacent non zero values
#             input_signal[x] = st.mean([input_signal[x - 1], input_signal[x + 1]])
#     return (input_signal)
#
#
# ## time stamp ##
# def get_time_from_timestamp(
#         timestamp):  ## converting unit timestamp to a readable date and time format
#     read_able = datetime.datetime.fromtimestamp(timestamp)
#     now_asia = str(read_able.astimezone(timezone('Asia/Kolkata')));  # print(now_asia)
#     year = now_asia[0:10]
#     year = year[8:10] + "-" + year[5:7] + "-" + year[0:4]
#     time = now_asia[11:19]
#     return (year, time)
#
#
# ## function extracts values from a dictionary given a specific index ##
# def Value_axis(data_dict, value):
#     data = list(data_dict.items())
#     an_array = np.array(data, dtype=object)
#     Values_1 = an_array[value]
#     Values_2 = np.asarray(Values_1[1])
#     return Values_2
#
#
# ## smoothening filter: Baseline filter+powerline noise+150 hz low pass filter ##
# def ecg_filters_V5_smooth(lead_data,
#                           hp=0.67):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#
#     for x in range(1, len(lead_data), 1):
#         if (lead_data[x] == 0):
#             lead_data[x] = lead_data[x - 1]
#
#     ##### Baseline filter without ST Segment distortion
#     b = signal.firwin(2377, cutoff=[hp / 500], window="hamming", pass_zero=False);
#     a = 1
#     # b = signal.firwin(1735, cutoff = [0.67/500], window = "hamming", pass_zero=False); a = 1
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     # h, g = signal.butter(2, [45/500] , btype='low', analog=False)
#     h, g = signal.butter(2, [45 / 500], btype='low', analog=False)
#     j, i = signal.butter(2, [15 / 500], btype='low', analog=False)
#
#     filt_BW = signal.filtfilt(b, a, lead_data);
#     filt_SM = (signal.lfilter(j, i, filt_BW))
#     filt_LP = (signal.lfilter(h, g, filt_BW))
#
#     sparse = filt_LP - filt_SM;
#     denoise = signal.savgol_filter(sparse, window_length=71, polyorder=3)
#     smooth_data = filt_SM + denoise
#
#     return smooth_data
#
#
# def baseline_filter(lead_data, hp=0.67):
#     ##### Baseline filter without ST Segment distortion
#     b = signal.firwin(2377, cutoff=[hp / 500], window="hamming", pass_zero=False);
#     a = 1
#     filt_BW = signal.filtfilt(b, a, lead_data);
#     return filt_BW
#
#
# def ecg_filters_V5_01(lead_data):
#
#
#     lead_data = np.asarray(lead_data, dtype=float).copy()
#
#     # Replace missing (zero) samples
#     for i in range(1, len(lead_data)):
#         if lead_data[i] == 0:
#             lead_data[i] = lead_data[i - 1]
#
#     Fs = 1000.0
#
#     # ------------------------------------------------------------------
#     # Baseline Wander Removal (High-pass FIR)
#     # ------------------------------------------------------------------
#     hp_coeff = signal.firwin(
#         numtaps=2377,
#         cutoff=1,
#         fs=Fs,
#         pass_zero=False,
#         window="hamming"
#     )
#
#     baseline_removed = signal.filtfilt(hp_coeff, [1.0], lead_data)
#
#     # ------------------------------------------------------------------
#     # 50 Hz Notch Filter
#     # ------------------------------------------------------------------
#     notch50_b, notch50_a = signal.iirnotch(
#         w0=50,
#         Q=30,
#         fs=Fs
#     )
#
#     filtered = signal.filtfilt(notch50_b, notch50_a, baseline_removed)
#
#     # ------------------------------------------------------------------
#     # 100 Hz Notch Filter
#     # ------------------------------------------------------------------
#     notch100_b, notch100_a = signal.iirnotch(
#         w0=100,
#         Q=30,
#         fs=Fs
#     )
#
#     filtered = signal.filtfilt(notch100_b, notch100_a, filtered)
#
#     # ------------------------------------------------------------------
#     # 45 Hz Low-pass Butterworth
#     # ------------------------------------------------------------------
#     lp_b, lp_a = signal.butter(
#         N=4,
#         Wn=45,
#         btype='low',
#         fs=Fs
#     )
#
#     filtered = signal.filtfilt(lp_b, lp_a, filtered)
#
#     # ------------------------------------------------------------------
#     # Optional smoothing
#     # ------------------------------------------------------------------
#     filtered = signal.savgol_filter(
#         filtered,
#         window_length=29,
#         polyorder=3
#     )
#
#     return filtered
#
#
# def phasor_transform(signal_in, Rv):
#     PT = np.empty_like(signal_in, dtype=float)
#     for i in range(len(signal_in)):
#         ch = signal_in[i] / Rv
#         PT[i] = math.degrees(math.atan(ch))
#         # PT[i] = Rv+ ()
#     return (PT)
#
#
# def find_PVC(signal_in, Rpeaks, Ramplitued, RRinterval):
#     ##   LIST OF AREA UNDER THE CURVE
#     AUC = collections.deque()
#     n_sig = len(signal_in)
#     for k in Rpeaks:
#
#
#         lo = max(0, k - 80)
#         hi = min(n_sig, k + 80)
#         ch = abs(np.trapz(signal_in[lo:hi]));
#         AUC.append(ch)
#         # print("median : ",ch, 1.3*st.median(AUC))
#     AUC = np.array(AUC);  # print(len(AUC),AUC)
#     ##    ECTOPIC FUNCTION
#     PVC_list = np.empty_like(Rpeaks, dtype=float);
#     PVC_list[:] = np.nan
#     SVC_list = np.empty_like(Rpeaks, dtype=float);
#     SVC_list[:] = np.nan
#     for i in range(1, len(RRinterval)):
#         if RRinterval[i] >= (1.5 * RRinterval[i - 1]):
#             if Ramplitued[i] >= (Ramplitued[i - 1] + (0.2 * Ramplitued[i - 1])):  # PVC
#
#                 if i > 1 and AUC[i] > (1.3 * st.median(AUC[0:i - 1])):
#                     PVC_list[i] = Rpeaks[i]
#             else:  # SVC or PVC
#                 if i > 1:
#                     if AUC[i] > (1.3 * st.median(AUC[0:i - 1])):
#                         PVC_list[i] = Rpeaks[i]
#     return np.array(PVC_list)
#
#
# def ecg_filters_Pwave(
#         lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     h, g = sp.signal.butter(4, [40 / 500], btype='low', analog=False);
#     j, i = signal.butter(2, [15 / 500], btype='low', analog=False)
#     filt_50 = sp.signal.lfilter(d, c, lead_data);
#     filt_100 = sp.signal.lfilter(f, e, filt_50)
#     filt_LP = sp.signal.lfilter(h, g, filt_50)
#     smoothed = sp.signal.savgol_filter(filt_LP, window_length=19, polyorder=3)
#     n = 41;
#     baseline_wander = sp.signal.medfilt(smoothed, kernel_size=n);
#     return baseline_wander
#
#
# def calculate_p_offset_p_duration_and_average_pr(p_loc, r_peak_ecg_signal, qrs_onset,
#                                                  window_size=60):
#     p_offset = []
#     p_duration = []
#     pr_differences = []
#
#     for n in range(len(p_loc)):
#         loc2 = p_loc[n] + window_size
#         p_start = r_peak_ecg_signal[loc2]
#         p_off = r_peak_ecg_signal[loc2 + 1]
#         count = 1
#
#         while p_start < p_off:
#             p_start = r_peak_ecg_signal[loc2 + count]
#             p_off = r_peak_ecg_signal[loc2 + 1 + count]
#             count = count + 1
#
#         p_offset.append(loc2 + count)
#         p_duration.append(qrs_onset[n] - (p_loc[n] - count))
#         pr_differences.append(qrs_onset[n] - (loc2 + count))
#
#     average_p_duration = np.mean(np.subtract(p_offset, p_loc))
#     average_pr = np.mean(pr_differences) + 80
#     if average_pr < 80:
#         average_pr = average_pr + 100
#     if average_pr < 0:
#         average_pr = average_pr + 150
#
#     return p_offset, p_duration, average_pr
#
#
# def detect_qrs_onset_offset(r_location, r_peak_ecg_signal, onset_window=50,
#                             offset_window=50):  ### onset offset change into 80 80 when bundle brunch block
#     qrs_onset = []
#     qrs_offset = []
#     signal_len = len(r_peak_ecg_signal)
#
#     for m in range(len(r_location)):
#         # QRS Onset
#         loc = r_location[m] - onset_window
#
#         if loc <= 0:
#
#             qrs_onset.append(0)
#         else:
#             R_peak = r_peak_ecg_signal[loc]
#             R_prev = r_peak_ecg_signal[loc - 1]
#             count = 1
#
#             while R_peak > R_prev and (loc - count - 1) >= 0:
#                 R_peak = r_peak_ecg_signal[loc - count]
#                 R_prev = r_peak_ecg_signal[loc - count - 1]
#                 count = count + 1
#
#             qrs_onset.append(max(0, loc - count))
#
#         # QRS Offset
#         loc2 = r_location[m] + offset_window
#
#         if loc2 >= signal_len - 1:
#             qrs_offset.append(signal_len - 1)
#         else:
#             R_on = r_peak_ecg_signal[loc2]
#             R_end = r_peak_ecg_signal[loc2 + 1]
#             count = 1
#
#             while R_on < R_end and (loc2 + count + 1) < signal_len:
#                 R_on = r_peak_ecg_signal[loc2 + count]
#                 R_end = r_peak_ecg_signal[loc2 + 1 + count]
#                 count = count + 1
#
#             qrs_offset.append(min(signal_len - 1, loc2 + count))
#
#     return qrs_onset, qrs_offset
#
#
# def calculate_pr_interval(r_location, qrs_onset, ecg_signal, offset_value=80):
#     r_loc_new = np.empty(len(r_location), dtype=int)
#     pr_array = np.empty(len(r_location))
#
#     for v in range(len(r_location)):
#         r_loc_new[v] = r_location[v] - offset_value
#         start = max(0, r_loc_new[v])
#         end = qrs_onset[v]
#
#         if start >= end:
#             start = max(0, end - 10)
#         if start >= end:
#
#             pr_array[v] = ecg_signal[end] if 0 <= end < len(ecg_signal) else 0.0
#         else:
#             pr_array[v] = np.mean(ecg_signal[start:end])
#
#     return r_loc_new, pr_array
#
#
# def calculate_mode(pr_array):
#     mode_value = st.mode(pr_array)
#     return mode_value
#
#
# def calculate_new_qrs_onset_offset(r_location, r_peak_ecg_signal, mode_value, offset_before=50,
#                                    offset_after=50):  ###  offset before and offset after  make it 80 80 if bundle bunch block
#     new_qrs_onset = []
#     new_qrs_offset = []
#     signal_len = len(r_peak_ecg_signal)
#
#     for a in range(len(r_location)):
#         # QRS Onset
#         loc = r_location[a] - offset_before
#         if loc <= 0:
#             new_qrs_onset.append(0)
#         else:
#             R_peak = r_peak_ecg_signal[loc]
#             R_prev = r_peak_ecg_signal[loc - 1]
#             count = 1
#             while R_peak > R_prev > mode_value and (loc - count - 1) >= 0:  # Added boundary check
#                 R_peak = r_peak_ecg_signal[loc - count]
#                 R_prev = r_peak_ecg_signal[loc - count - 1]
#                 count = count + 1
#             new_qrs_onset.append(max(0, loc - count + 1))  # Adjusted for consistency
#
#     for b in range(len(r_location)):
#         # QRS Offset
#         loc2 = r_location[b] + offset_after
#         if loc2 >= signal_len - 1:  # If loc2 is already at or beyond the last valid index, the offset is the last index.
#             new_qrs_offset.append(signal_len - 1)
#         else:
#             # Check loc2 + 1 before accessing it in r_end
#             if loc2 + 1 >= signal_len:
#                 new_qrs_offset.append(
#                     signal_len - 1)  # If loc2 is valid but loc2+1 is not, offset is the last index
#             else:
#                 r_on = r_peak_ecg_signal[loc2]
#                 r_end = r_peak_ecg_signal[loc2 + 1]
#                 count = 1
#                 while r_on < r_end < mode_value and (
#                         loc2 + count + 1) < signal_len:  # Added boundary check
#                     r_on = r_peak_ecg_signal[loc2 + count]
#                     r_end = r_peak_ecg_signal[loc2 + 1 + count]
#                     count = count + 1
#                 new_qrs_offset.append(
#                     min(signal_len - 1, loc2 + count - 1))  # Adjusted for consistency
#
#     return new_qrs_onset, new_qrs_offset
#
#
# def R_Peak_Detection_05(raw_data):
#     peaks_up = [];
#     amp = [];
#     peaks_up_s = [];
#     ecp = [];
#     b = signal.firwin(1377, cutoff=[2 / 500], window="hamming", pass_zero=False);
#     a = 1
#     h, g = signal.butter(4, [45 / 500], btype='low', analog=False)
#     filt_BW = signal.filtfilt(b, a, raw_data);
#     data_F = signal.lfilter(h, g, filt_BW)
#     data_D = np.diff(data_F);
#     data_S = data_D * data_D
#
#     robust_peak_ref = np.percentile(data_S, 99.5)
#     peaks, rpeak = signal.find_peaks(data_S, distance=255, height=(robust_peak_ref / 1.7));
#     n = len(peaks);
#
#     for i in range(0, n):
#         win = 70;
#         if (peaks[i] - (win)) < 0:
#             start_win = 0
#         else:
#             start_win = (peaks[i] - (win))
#         seg = data_F[start_win:(peaks[i]) + (
#             win)];  # seg = seg*seg; ############## Multiple Seg for getting the R or S peak
#         max_peak, peak_amp = signal.find_peaks(seg, distance=10, height=(max(seg) / 2))
#
#         if len(max_peak) == 1:
#             peaks_u = start_win + max_peak[0]
#         else:
#             peaks_u = start_win + np.argmax(seg)
#         peaks_up.append(peaks_u);
#
#         seg_s = seg * (-1);
#         max_peak, peak_amp = signal.find_peaks(seg_s, distance=10)  # , height = (max(seg_s)/2))
#         if len(max_peak) == 1:
#             peaks_u = start_win + max_peak[0]  ########### need fix for short S wave
#         else:
#             peaks_u = start_win + np.argmax(seg_s)
#         peaks_up_s.append(peaks_u);
#
#     rr_int = np.diff(peaks_up);
#     th_ep = st.mean(rr_int) + st.mean(rr_int) / 10
#
#     for j in range(0, len(rr_int)):
#         if rr_int[j] > th_ep:
#             ep_seg = (data_F[peaks_up[j] + 60:peaks_up[j + 1] - 60])
#             max_e = np.argmax(ep_seg * ep_seg)  ########### for irregularity needs to be fixed
#             max_e = max_e + peaks_up[j] + 60;
#             # if ep_data[max_e] > (data_F[peaks_up[j]]/2):
#             ecp.append(max_e)
#     r_peak_values = [data_F[peak] for peak in peaks_up]
#     return peaks_up, ecp, peaks_up_s
#
#
# def P_Detection(rawECG, R_peaks, RRint, Ramp):
#     Filt_ECG = ecg_filters_V5_smooth(rawECG)
#     peaks = R_peaks;
#
#     PVC = find_PVC(Filt_ECG, peaks, Ramp, RRint)
#     PT_pwave = phasor_transform(ecg_filters_Pwave(rawECG), Rv=0.05)
#     P_st = [];
#     P_sp = [];
#     loc = [];
#     Pamp = []
#     P_location = [];
#     Pst = [];
#     Psp = []
#     for k in range(0, len(peaks)):
#         if k == 0:  # 1st P-peak detection
#             win_1 = len(Filt_ECG[0:peaks[0]]);  # print(win_1)
#             if win_1 > 300:
#                 win_st = peaks[0] - 300;
#                 win_sp = peaks[0] - 80
#                 ch = np.max(PT_pwave[peaks[0] - 300:peaks[0] - 80]);
#             elif win_1 <= 300 and win_1 >= 80:
#                 win_st = 0;
#                 win_sp = peaks[0] - 80
#                 ch = np.max(PT_pwave[0:peaks[0] - 80]);
#             elif win_1 < 80:
#                 P_location.append(np.nan);
#                 win_st = np.nan;
#                 win_sp = np.nan
#                 break;
#         elif k > 0 and k < len(peaks) - 1:  # Second to Last but 1 peak
#             win_st = int(peaks[k] - (0.4 * RRint[k]));
#             win_sp = int(peaks[k] - (0.05 * (RRint[k] - 100)));  # print(win_st,win_sp)
#             ch = np.max(PT_pwave[win_st:win_sp]);
#         else:  # Last peak
#             win_st = int(peaks[k] - (0.4 * RRint[k - 1]));
#             win_sp = int(peaks[k] - (0.05 * (RRint[k - 1] - 100)));  # print(win_st,win_sp)
#             ch = np.max(PT_pwave[win_st:win_sp]);
#         loc_ch = (np.where(PT_pwave == ch));
#         loc_ch = loc_ch[0];
#         if loc_ch[0] < 80:
#             loc_st = loc_ch[0]
#         else:
#             loc_st = loc_ch[0] - 80
#         ix = np.max(Filt_ECG[loc_st:loc_ch[0] + 80]);
#         loc_ch = (np.where(Filt_ECG == ix));
#         loc_ch = loc_ch[0];
#         loc.append(loc_ch[0]);
#         Pamp.append(np.abs(Filt_ECG[loc_ch[0]]))
#         P_st.append(win_st);
#         P_sp.append(win_sp)
#
#     # print("Pamp temp :", len(Pamp), Pamp);  print("Ploc temp :", len(loc), loc)
#
#     for m in range(0, len(RRint)):
#         # print(m)
#         if np.isnan(PVC[m]) == True and Pamp[m] > (
#                 0.5 * Filt_ECG[peaks[m]]):  # Beat Not a PVC: retain P loc
#             P_location.append(loc[m])
#             Pst.append(P_st[m]);
#             Psp.append(P_sp[m])
#
#     P_location = loc
#     P_onset = [];  # print("P location : ",P_location)
#
#     MAX_P_ONSET_SEARCH = 150  # samples (~150ms at 1000Hz)
#     for m in range(len(P_location)):
#         loc = P_location[m]
#         Count = 1
#         if (loc - 20 - 1) < 0:
#
#             P_onset.append(max(0, loc - 20))
#             continue
#         T_on = Filt_ECG[loc - 0]
#         prev = Filt_ECG[loc - 20 - 1]
#         while T_on > prev:
#             next_idx_a = loc - 0 - Count
#             next_idx_b = loc - 20 - Count - 1
#             if next_idx_b < 0 or Count >= MAX_P_ONSET_SEARCH:
#
#                 break
#             T_on = Filt_ECG[next_idx_a]
#             prev = Filt_ECG[next_idx_b]
#             Count = Count + 1
#         P_onset.append(loc - Count);
#     return (P_location, Pamp, P_onset)
#
#
# def ecg_filters_Twave(
#         lead_data):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
#     ##### Powerline noise and its harmonics removal
#     Fs = 1000;
#     F0 = 50;
#     F1 = F0 * 2;
#     r = 1 - ((3.14 * 2) / 1000);
#     W0 = (2 * 3.14 * F0) / Fs;
#     W1 = (2 * 3.14 * F1) / Fs
#     d0 = 1;
#     d1 = -2 * (np.cos(W0));
#     d2 = 1;
#     c2 = 1;
#     c1 = -2 * r * (np.cos(W0));
#     c0 = r * r;
#     d = [d2, d1, d0];
#     c = [c2, c1, c0]
#     f0 = 1;
#     f1 = -2 * (np.cos(W1));
#     f2 = 1;
#     e2 = 1;
#     e1 = -2 * r * (np.cos(W1));
#     e0 = r * r;
#     f = [f2, f1, f0];
#     e = [e2, e1, e0]
#     ##### 150Hz Low pass filter
#     h, g = sp.signal.butter(4, [20 / 500], btype='low', analog=False)
#     j, i = sp.signal.butter(2, [15 / 500], btype='low', analog=False)
#     filt_50 = sp.signal.lfilter(d, c, lead_data);  # filt_100 = sp.signal.lfilter(f, e, filt_50)
#     filt_LP = sp.signal.lfilter(h, g, filt_50)
#     smoothed = sp.signal.savgol_filter(filt_LP, window_length=19, polyorder=3)
#     n = 41;
#     baseline_wander = sp.signal.medfilt(smoothed, kernel_size=n);
#     return baseline_wander
#
#
# def T_Detection(raw_signal, R_peaks, RRint):
#     Filt_ECG = ecg_filters_V5_smooth(raw_signal)
#     T_filt = ecg_filters_Twave(raw_signal)
#     PT_twave = phasor_transform(T_filt, Rv=0.1)
#     T_st = [];
#     T_sp = [];
#     T_loc = [];
#     Tamp = []
#     for k in range(0, len(R_peaks)):
#         if k == len(R_peaks) - 1:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k - 1]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k - 1])));  # print(win_st,win_sp)
#         elif k > 0 and k < len(R_peaks) - 1:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k])));  # print(win_st,win_sp)
#         else:
#             win_st = int(R_peaks[k] + (0.16 * RRint[k + 1]));
#             win_sp = int(R_peaks[k] + (0.57 * (RRint[k + 1])));  # print(win_st,win_sp)
#
#         sig_len = len(PT_twave)
#         win_st_c = max(0, min(win_st, sig_len))
#         win_sp_c = max(0, min(win_sp, sig_len))
#         if win_st_c >= win_sp_c:
#             print(f"Warning: Empty slice for R-peak index {k}. win_st: {win_st}, win_sp: {win_sp}")
#             # Skip this iteration if the slice is empty
#             continue
#         else:
#             ch = np.max(PT_twave[win_st_c:win_sp_c])
#         loc_ch = (np.where(PT_twave == ch));
#         loc_ch = loc_ch[0];
#
#         if len(loc_ch) > 0:
#
#             ecg_len = len(Filt_ECG)
#             lo = max(0, loc_ch[0] - 80)
#             hi = min(ecg_len, loc_ch[0] + 80)
#             if hi <= lo:
#                 print(f"Warning: Empty ECG window for R-peak index {k} "
#                       f"(loc_ch[0]={loc_ch[0]}, buffer length {ecg_len}). Skipping.")
#                 continue
#             ix = np.max(Filt_ECG[lo:hi]);
#             loc_ch = (np.where(Filt_ECG == ix));
#             loc_ch = loc_ch[0];
#             if len(loc_ch) == 0:
#                 print(f"Warning: no matching Filt_ECG index for R-peak index {k}. Skipping.")
#                 continue
#             T_loc.append(loc_ch[0]);
#             Tamp.append(np.abs(Filt_ECG[loc_ch[0]]))
#             T_st.append(win_st);
#             T_sp.append(win_sp)
#         else:
#             print(f"Warning: loc_ch is empty for R-peak index {k}. Skipping this iteration.")
#             # Handle the case where loc_ch is empty, e.g., append NaN to T_loc, etc.
#
#     return (np.array(T_loc))
#
#
# def calculate_t_offset(t_loc, r_peak_ecg_signal):
#     t_offset = []
#     signal_length = len(r_peak_ecg_signal)
#
#     for o in range(len(t_loc)):
#         location = t_loc[o]
#         t_start = r_peak_ecg_signal[location]
#         t_off = r_peak_ecg_signal[min(location + 1, signal_length - 1)]
#         count = 1
#
#         while t_start < t_off and location + count + 1 < signal_length:
#             t_start = r_peak_ecg_signal[min(location + count, signal_length - 1)]
#             t_off = r_peak_ecg_signal[min(location + count + 1, signal_length - 1)]
#             count += 1
#
#         # Ensure the offset doesn't go out of bounds
#         final_offset = min(location + count + 100, signal_length - 1)
#         t_offset.append(final_offset)
#
#     return t_offset
#
#
# def calculate_snr_db(raw_signal, filtered_signal):
#
#     signal_power = np.mean(filtered_signal ** 2)
#     noise = raw_signal - filtered_signal
#     noise_power = np.mean(noise ** 2)
#
#     eps = 1e-12
#     if noise_power > eps:
#         return 10 * np.log10(max(signal_power, eps) / noise_power)
#     else:
#         return float("inf")
#
#
# def bandpass(signal_in, fs, lowcut=30, highcut=100, order=4):
#
#     nyq = 0.5 * fs
#     b, a = butter(order, [lowcut / nyq, highcut / nyq], btype='band')
#     return filtfilt(b, a, signal_in)
#
#
#
#
# def compute_his_bundle_intervals(v2, fs=1000, lsb_V=286e-9):
#
#
#     ###--------------------------- Raw_data fetching and R peak detection ----------------------------###
#     R_data = v2 * (-1)
#     R_peak_ecg_signal = ecg_filters_V5_01(R_data)
#     R_Location, ectopic, S_Location = R_Peak_Detection_05(R_data)
#     R_Location = np.array(R_Location, dtype=int)
#
#
#     R_peak_ecg_signal_meas = R_peak_ecg_signal
#     R_Location_meas = R_Location
#
#     if len(R_Location) < 3:
#         raise ValueError("Not enough R peaks detected in this buffer.")
#
#     ###--------------------------Heart Rate calculation---------------------------------- ###
#     R_amp = [];
#     RRint = [];
#     for i in range(0, len(R_Location) - 1):
#         ch = R_Location[i + 1] - R_Location[i];
#         RRint.append(ch)
#         R_amp.append(R_peak_ecg_signal[R_Location[i]])
#         if i == (len(R_Location) - 2):
#             R_amp.append(R_peak_ecg_signal[R_Location[i + 1]])
#
#     QRS_Onset, QRS_Offset = detect_qrs_onset_offset(R_Location, R_peak_ecg_signal)
#     R_Loc_New, PR_Array = calculate_pr_interval(R_Location, QRS_Onset, R_peak_ecg_signal)
#     Mode_Value = calculate_mode(PR_Array)
#     New_QRS_Onset, New_QRS_Offset = calculate_new_qrs_onset_offset(R_Location, R_peak_ecg_signal,
#                                                                    Mode_Value)
#     P_location, Pamp, P_onset = P_Detection(R_data, R_Location, RRint, R_amp)
#
#     filtered_v2 = ecg_filters_V5_smooth(v2)
#     R_data = filtered_v2 * (-1)
#
#     # ---------- Whole-buffer SNR ----------
#     # Matched to the Colab reference: compares the ORIGINAL raw signal
#     # against the SMOOTHED signal across the entire buffer, not just
#     # the one selected beat's tiny His-bundle window. This measures
#     # overall recording quality rather than one beat's local quality.
#     raw_signal = v2 * (-1)
#     filtered_signal = R_data
#     SNR_dB = calculate_snr_db(raw_signal, filtered_signal)
#
#     R_peak_ecg_signal = ecg_filters_V5_smooth(R_data)
#     R_Location, ectopic, S_Location = R_Peak_Detection_05(R_data)
#     T_loc = T_Detection(R_peak_ecg_signal, R_Location, RRint)
#     T_Offset = calculate_t_offset(T_loc, R_peak_ecg_signal)
#
#     T_Offset = np.array(T_Offset, dtype=int)
#     P_location = np.array(P_location, dtype=int)
#     P_onset = np.array(P_onset, dtype=int)
#
#     ads_values_tloc_p_location = []
#     ads_seg_bounds = []  # (seg_start, seg_end) per candidate, needed for the diagnostic SNR below
#     for i, t_offset in enumerate(T_Offset):
#         next_p_onset = next((p for p in P_onset if p > t_offset), None)
#         if next_p_onset is not None and next_p_onset < len(R_peak_ecg_signal):
#             seg_start = int(t_offset)
#             seg_end = int(next_p_onset)
#             ads_segment = np.round(np.abs(R_peak_ecg_signal[seg_start:seg_end] / 0.000286)).astype(int)
#             ads_values_tloc_p_location.append(ads_segment)
#             ads_seg_bounds.append((seg_start, seg_end))
#
#     if not ads_values_tloc_p_location:
#         raise ValueError("No T→P segments found for ADS analysis.")
#
#
#     #                SELECT BEST SEGMENTS (by RMS voltage)
#
#
#     segment_scores = []
#     segment_indices = []
#     diagnostic_snr_by_index = {}
#
#     for i, ads_segment in enumerate(ads_values_tloc_p_location):
#         ads_segment_f = np.asarray(ads_segment, dtype=float)
#         if len(ads_segment_f) < 10:  # ignore very small segments
#             continue
#         voltage = ads_segment_f * lsb_V
#         rms = np.sqrt(np.mean(voltage ** 2))
#         segment_scores.append(rms)
#         segment_indices.append(i)
#
#         # Diagnostic-only per-beat SNR (does not affect selection).
#         seg_start, seg_end = ads_seg_bounds[i]
#         raw_local = raw_signal[seg_start:seg_end]
#         filt_local = filtered_signal[seg_start:seg_end]
#         if len(raw_local) >= 10 and len(filt_local) >= 10:
#             diagnostic_snr_by_index[i] = calculate_snr_db(raw_local, filt_local)
#
#     segment_scores = np.array(segment_scores)
#     segment_indices = np.array(segment_indices)
#
#     if len(segment_indices) == 0:
#         raise ValueError("No valid segments (all shorter than 10 samples) for ADS analysis.")
#
#     # Sort DESCENDING: highest-RMS beat tried first (matches Colab).
#     sorted_order = np.argsort(segment_scores)[::-1]
#     candidate_indices = segment_indices[sorted_order]  # ALL candidates, best (highest RMS) first
#
#
#     print("\n--- Per-beat selection diagnostics (RMS = used, SNR = logged only) ---")
#     for rank, idx in enumerate(candidate_indices):
#         rms_val = segment_scores[sorted_order[rank]]
#         snr_val = diagnostic_snr_by_index.get(int(idx), float("nan"))
#         marker = "  <-- SELECTED (if it passes validation)" if rank == 0 else ""
#         print(f"  rank {rank}: beat #{idx}  RMS={rms_val:.6f} V  SNR={snr_val:.2f} dB{marker}")
#     print("--- end diagnostics ---\n")
#
#
#     last_error = None
#     result_core = None
#
#     for candidate_idx in candidate_indices:
#         try:
#             result_core = _evaluate_candidate_beat(
#                 candidate_idx,
#                 P_onset=P_onset,
#                 R_Location=R_Location_meas,
#                 New_QRS_Onset=New_QRS_Onset,
#                 New_QRS_Offset=New_QRS_Offset,
#                 R_peak_ecg_signal=R_peak_ecg_signal_meas,
#                 fs=fs,
#             )
#             break
#         except ValueError as e:
#             last_error = e
#             continue
#
#     if result_core is None:
#         raise ValueError(
#             f"No usable beat found among {len(candidate_indices)} candidates "
#             f"(last error: {last_error})."
#         )
#
#
#     result_core["SNR"] = float(SNR_dB)
#
#     return result_core
#
#
# def _evaluate_candidate_beat(
#         candidate_idx,
#         P_onset,
#         R_Location,
#         New_QRS_Onset,
#         New_QRS_Offset,
#         R_peak_ecg_signal,
#         fs,
# ):
#
#
#     if candidate_idx >= len(P_onset) or candidate_idx >= len(R_Location):
#         raise ValueError("Candidate segment index out of range for P_onset / R_Location.")
#
#     R_peak_i = int(R_Location[candidate_idx])
#
#     # ---------- QRS onset ----------
#     qrs_candidates = [q for q in New_QRS_Onset if q < R_peak_i]
#     if len(qrs_candidates) == 0:
#         raise ValueError("No QRS onset found before this candidate's R peak.")
#     QRS_on_i = qrs_candidates[-1]
#
#     # ---------- QRS offset ----------
#     qrs_off_candidates = [q for q in New_QRS_Offset if q > R_peak_i]
#     if len(qrs_off_candidates) == 0:
#         raise ValueError("No QRS offset found after this candidate's R peak.")
#     QRS_off_i = qrs_off_candidates[0]
#
#     # ---------- P onset for this beat ----------
#     p_on_i = int(P_onset[candidate_idx])
#     if p_on_i >= QRS_on_i:
#         raise ValueError("P-onset is not before QRS-onset for this candidate beat.")
#
#     # ---------- Bandpass the P-onset -> QRS-onset segment ----------
#     seg_start = p_on_i
#     seg_end = QRS_on_i + 1
#     raw_segment = R_peak_ecg_signal[seg_start:seg_end]
#
#     if len(raw_segment) <= 10:
#         raise ValueError("P-onset to QRS-onset segment too short to bandpass filter.")
#
#     bp = bandpass(raw_segment, fs)
#
#
#     bp_abs_max = np.max(np.abs(bp))
#     if bp_abs_max > 0:
#         bp_norm = bp / bp_abs_max
#         scale = 0.5 * np.max(np.abs(raw_segment))
#         bp_scaled = bp_norm * scale
#     else:
#         bp_scaled = bp
#
#     # ---------- A wave (true peak in early PR region) ----------
#     qrs_local = QRS_on_i - seg_start
#     search_end = int(0.45 * qrs_local)
#     sub = bp[15:search_end]
#     if len(sub) == 0:
#         raise ValueError("A-wave search window is empty for this candidate beat.")
#     peak_idx = np.argmax(sub)
#     A_wave_i = seg_start + 15 + peak_idx
#
#     # -------------------------------------------------------
#     #        H_on and H_off DETECTION (TRUE BIPHASIC START)
#     # -------------------------------------------------------
#     H_on_i = None
#     bp_segment = bp  # bandpassed signal
#
#     # search region before QRS (where the circled wave is)
#     search_start = max(0, qrs_local - 60)
#     search_end = qrs_local - 5
#     sub = bp_segment[search_start:search_end]
#
#     # Step 1: find zero crossing (center of biphasic)
#     zc_idx = None
#     for i in range(1, len(sub)):
#         if sub[i - 1] < 0 and sub[i] > 0:
#             zc_idx = i
#             break
#
#
#     if zc_idx is not None:
#         for i in range(zc_idx, 1, -1):
#             # beginning = where slope starts increasing
#             if abs(sub[i] - sub[i - 1]) < 0.001:
#                 H_on_i = seg_start + search_start + i
#                 break
#
#     # search between H_on and QRS
#     H_off_i = None
#     if H_on_i is not None:
#         h_on_local = H_on_i - seg_start
#
#         search_start = h_on_local + 5
#         search_end = qrs_local - 2
#
#         sub = bp_segment[search_start:search_end]
#
#         if len(sub) > 5:
#             # find LAST zero-crossing before QRS
#             for i in range(len(sub) - 1, 1, -1):
#                 if sub[i - 1] > 0 and sub[i] < 0:
#                     H_off_i = seg_start + search_start + i
#                     break
#
#
#     PA_ms = A_wave_i - p_on_i
#     if H_on_i is not None:
#         AH_ms = H_on_i - A_wave_i
#         HV_ms = QRS_on_i - H_on_i
#     else:
#         AH_ms = float("nan")
#         HV_ms = float("nan")
#     PR_ms = QRS_on_i - p_on_i
#     QRS_ms = QRS_off_i - QRS_on_i
#
#     return {
#         "PA": float(PA_ms),
#         "AH": float(AH_ms),
#         "HV": float(HV_ms),
#         "PR": float(PR_ms),
#         "QRS": float(QRS_ms),
#
#         "SNR": None,
#
#         "R_peak_i": R_peak_i,
#         "p_on_i": p_on_i,
#         "A_wave_i": A_wave_i,
#         "H_on_i": H_on_i,
#         "H_off_i": H_off_i,
#         "QRS_on_i": QRS_on_i,
#         "QRS_off_i": QRS_off_i,
#         "seg_start": seg_start,
#         "seg_end": seg_end,
#         "bp_scaled": bp_scaled,
#     }
#
#
# def analyzelead4(samples, graph_path):
#
#     try:
#         import matplotlib
#         matplotlib.use("Agg")
#         import matplotlib.pyplot as plt
#
#         fs = 1000
#
#         # ----------------------------------
#         # Convert Java ArrayList to Python list
#         # ----------------------------------
#         try:
#             # Direct conversion from Java ArrayList to NumPy (faster)
#             size = samples.size()
#             lead4 = np.zeros(size, dtype=np.float64)
#             for i in range(size):
#                 lead4[i] = float(samples.get(i))
#         except Exception:
#             # Already Python list
#             lead4 = np.array([float(x) for x in samples], dtype=np.float64)
#
#         # Quick validation
#         if len(lead4) == 0:
#             return [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ""]
#
#         # ----------------------------------
#         # Convert Lead4 to V2 (mV)
#         # ----------------------------------
#         gain = 1
#         scale = 0.000286
#         v2 = ((lead4) * scale) * (-(gain))
#
#         # ----------------------------------
#         # Real interval detection (same algorithm process() uses)
#         # ----------------------------------
#         result = compute_his_bundle_intervals(v2, fs=fs)
#
#
#         # Generate Graph
#
#         fig, axes = plt.subplots(2, 1, figsize=(12, 9))
#
#         axes[0].plot(v2, color='blue', linewidth=0.8)
#         axes[0].set_title('Full ECG Trace (Lead4 / V2)')
#         axes[0].set_xlabel('Sample')
#         axes[0].set_ylabel('Amplitude (mV)')
#         axes[0].grid(True)
#
#         margin = 150
#         zoom_start = max(0, result["seg_start"] - margin)
#         zoom_end = min(len(v2), result["QRS_off_i"] + margin)
#
#         if zoom_end > zoom_start:
#             x_zoom = np.arange(zoom_start, zoom_end)
#             axes[1].plot(x_zoom, v2[zoom_start:zoom_end], color='black', linewidth=1.0,
#                          label='Raw beat')
#
#
#             bp_scaled = result.get("bp_scaled")
#             if bp_scaled is not None and len(bp_scaled) > 0:
#                 x_bp = np.arange(result["seg_start"], result["seg_start"] + len(bp_scaled))
#                 axes[1].plot(x_bp, bp_scaled, color='teal', linewidth=1.2, linestyle='-',
#                              alpha=0.85, label='Bandpassed His signal (scaled)')
#
#             landmarks = [
#                 ('P-onset', result["p_on_i"], 'green'),
#                 ('A-wave', result["A_wave_i"], 'orange'),
#                 ('H-onset', result["H_on_i"], 'purple'),
#                 ('H-offset', result["H_off_i"], 'brown'),
#                 ('QRS-onset', result["QRS_on_i"], 'red'),
#                 ('QRS-offset', result["QRS_off_i"], 'red'),
#                 ('R-peak', result["R_peak_i"], 'blue'),
#             ]
#
#             y_top = np.max(v2[zoom_start:zoom_end])
#             for label, idx, color in landmarks:
#
#                 if idx is not None and zoom_start <= idx < zoom_end:
#                     axes[1].axvline(idx, color=color, linestyle='--', linewidth=1)
#                     axes[1].text(idx, y_top, label, rotation=90,
#                                  va='top', ha='right', fontsize=8, color=color)
#
#             axes[1].legend(loc='lower right', fontsize=8)
#
#         axes[1].set_title('Best His Bundle Segment (Detected Landmarks)')
#         axes[1].set_xlabel('Sample')
#         axes[1].set_ylabel('Amplitude (mV)')
#         axes[1].grid(True)
#
#         plt.tight_layout()
#         plt.savefig(graph_path, dpi=150, bbox_inches='tight')
#         plt.close()
#
#         # ----------------------------------
#         # Return: [PA, AH, HV, PR, QRS, SNR, graph_path]
#         # ----------------------------------
#         return [
#             result["PA"],
#             result["AH"],
#             result["HV"],
#             result["PR"],
#             result["QRS"],
#             result["SNR"],
#             str(graph_path)
#         ]
#
#     except Exception as e:
#         import traceback
#         print("================================")
#         print("PYTHON ERROR in analyzelead4")
#         print(str(e))
#         traceback.print_exc()
#         print("================================")
#         return [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ""]
#
#
# def process(buffer2):
#
#     fs = 1000
#
#     v2 = np.array(buffer2, dtype=np.float64)
#     gain = 1
#     scale = 0.000286
#     # Convert to mV and invert
#     v2 = ((v2) * scale) * (-(gain));
#
#     result = compute_his_bundle_intervals(v2, fs=fs)
#
#     PA_ms = result["PA"]
#     AH_ms = result["AH"]
#     HV_ms = result["HV"]
#     PR_ms = result["PR"]
#     QRS_ms = result["QRS"]
#     SNR_dB = result["SNR"]
#
#     ### Display #####
#     df = pd.DataFrame(
#         [[int(PA_ms), int(AH_ms), int(HV_ms), int(PR_ms), int(QRS_ms)]],
#         columns=[
#             "PA (ms)",
#             "AH (ms)",
#             "HV (ms)",
#             "PR (ms)",
#             "QRS (ms)"
#         ]
#     )
#
#     # ANSI escape codes
#     BOLD = "\033[1m"
#     BLACK = "\033[30m"
#     RESET = "\033[0m"
#     RED = "\033[91m"
#     BLUE = "\033[94m"
#     print("\n")
#     print(BOLD + BLACK + "=" * 60 + RESET)
#     print(BOLD + BLACK + "           BEST  HIS BUNDLE SEGMENT" + RESET)
#     print(BOLD + BLACK + "=" * 60 + RESET)
#     print()
#     print(BOLD + df.to_string(index=False) + RESET)
#     print(BOLD + BLACK + "=" * 60 + RESET)
#     print()
#
#     snr_color = RED if SNR_dB < 6 else BLUE
#     print(f"{BOLD}{snr_color}SNR of Best Segment : {SNR_dB:.2f} dB{RESET}")
#     print(BOLD + BLACK + "=" * 60 + RESET)
#
#     return {
#         "PA": PA_ms,
#         "AH": AH_ms,
#         "HV": HV_ms,
#         "PR": PR_ms,
#         "QRS": QRS_ms,
#         "SNR": SNR_dB
#     }




#this code r peak marking corretely





## ============================================================
## FIXED VERSION 2
## Changes in this revision:
##   1. HH value removed everywhere (no longer computed, no
##      longer returned to Android, no longer in the debug table).
##   2. THE MAIN BUG: analyzelead4() -- the function Android
##      actually calls -- was NOT using the real PA/AH/HV
##      detection algorithm. It was using placeholder guesses:
##          AH_ms = PR_ms * 0.3
##          HV_ms = QRS_ms * 0.2
##      These are not measurements, just arbitrary fractions of
##      PR/QRS, which is why the on-device numbers were wrong.
##      The *real* algorithm (bandpass filter -> A-wave peak ->
##      H-on/H-off zero-crossing detection) only existed inside
##      process(), which the app never called.
##   3. Fix: the real algorithm from process() has been pulled
##      out into one shared function,
##      compute_his_bundle_intervals(), and BOTH process() and
##      analyzelead4() now call it. So the values Android
##      receives are the same real measurement, not a guess.
##   4. No thresholds, window sizes, filter cutoffs, or any other
##      numeric parameter/logic used by the real algorithm were
##      changed -- only removed the fake approximations and wired
##      up the real one instead.
##   5. R_Peak_Detection_05: fixed IndexError crash.
##      "len(max_peak) < 2" matched BOTH zero peaks found AND one
##      peak found, then unconditionally indexed max_peak[0] --
##      which throws IndexError on an empty array. find_peaks()
##      requires a true local max with lower neighbors on both
##      sides, so a flat/noisy 140-sample window can legitimately
##      return zero peaks. Changed to "== 1" so only the true
##      single-peak case indexes max_peak[0]; zero-peak and
##      multi-peak cases both fall back to argmax(seg), same as
##      the pre-existing multi-peak fallback. Applied to both the
##      R-peak block and the mirrored S-wave block.
##   6. R_Peak_Detection_05: replaced the hardcoded threshold
##      window max(data_S[1000:5000]) with a whole-buffer robust
##      percentile (99.5th) so short buffers, or buffers where
##      the first ~4s happens to be noisy/quiet, don't miscalibrate
##      the peak-detection threshold for the entire buffer.
## ============================================================

import math
import collections
import datetime
import numpy as np
import scipy as sp
from scipy import signal
from scipy.signal import butter, filtfilt
import statistics as st
import pandas as pd
from pytz import timezone

# condat_tv is no longer imported or used: ecg_filters_V5_smooth() now
# always denoises via Savitzky-Golay (matching the colleague-improved
# Colab version) instead of conditionally calling condat_tv.tv_denoise()
# when that native library happens to be bundled. This removes the
# dependency entirely rather than silently degrading when it's missing.


## data loss correction ##

def data_loss(input_signal):
    for x in range(1, len(input_signal) - 1, 1):  #### data loss
        if (input_signal[
            x] == 0):  ## checks zero values in input signal and replaces with the mean of adjacent non zero values
            input_signal[x] = st.mean([input_signal[x - 1], input_signal[x + 1]])
    return (input_signal)


## time stamp ##
def get_time_from_timestamp(
        timestamp):  ## converting unit timestamp to a readable date and time format
    read_able = datetime.datetime.fromtimestamp(timestamp)
    now_asia = str(read_able.astimezone(timezone('Asia/Kolkata')));  # print(now_asia)
    year = now_asia[0:10]
    year = year[8:10] + "-" + year[5:7] + "-" + year[0:4]
    time = now_asia[11:19]
    return (year, time)


## function extracts values from a dictionary given a specific index ##
def Value_axis(data_dict, value):
    data = list(data_dict.items())
    an_array = np.array(data, dtype=object)
    Values_1 = an_array[value]
    Values_2 = np.asarray(Values_1[1])
    return Values_2


## smoothening filter: Baseline filter+powerline noise+150 hz low pass filter ##
def ecg_filters_V5_smooth(lead_data,
                          hp=0.67):  #### According to IEC 60601-2-25 (BW, 50, 100 and LP) with smoothing
    # Denoising step updated to match the colleague-improved Colab
    # version: the sparse/high-frequency component is now ALWAYS
    # smoothed via Savitzky-Golay (window_length=71), instead of
    # conditionally calling condat_tv.tv_denoise() when that native
    # library happens to be available and silently skipping denoising
    # entirely (denoise = sparse) when it isn't. Every test run in this
    # conversation printed "condat_tv not available on this device",
    # meaning that fallback (no denoising at all) was very likely the
    # ACTIVE path on the real device this whole time, not an edge case -
    # this closes that gap without any native-library dependency.
    #
    # NOTE: the function name is kept as ecg_filters_V5_smooth (not
    # renamed to ecg_filters_V5_smooth_without_condat_tv as in the
    # colleague's version) so every existing call site in this file
    # keeps working automatically. The colleague's version renamed the
    # function but left two call sites still referencing the OLD name,
    # which is undefined once the old function is removed - that
    # produces a NameError when run as a standalone module (exactly how
    # Chaquopy loads this file on Android), even though it can appear to
    # "work" inside a Colab notebook, where an earlier cell's leftover
    # definition of the old name is still sitting in memory. Keeping one
    # name here avoids that class of bug entirely.
    for x in range(1, len(lead_data), 1):
        if (lead_data[x] == 0):
            lead_data[x] = lead_data[x - 1]

    ##### Baseline filter without ST Segment distortion
    b = signal.firwin(2377, cutoff=[hp / 500], window="hamming", pass_zero=False);
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
    # h, g = signal.butter(2, [45/500] , btype='low', analog=False)
    h, g = signal.butter(2, [45 / 500], btype='low', analog=False)
    j, i = signal.butter(2, [15 / 500], btype='low', analog=False)

    filt_BW = signal.filtfilt(b, a, lead_data);
    filt_SM = (signal.lfilter(j, i, filt_BW))
    filt_LP = (signal.lfilter(h, g, filt_BW))

    sparse = filt_LP - filt_SM;
    denoise = signal.savgol_filter(sparse, window_length=71, polyorder=3)
    smooth_data = filt_SM + denoise

    return smooth_data


def baseline_filter(lead_data, hp=0.67):
    ##### Baseline filter without ST Segment distortion
    b = signal.firwin(2377, cutoff=[hp / 500], window="hamming", pass_zero=False);
    a = 1
    filt_BW = signal.filtfilt(b, a, lead_data);
    return filt_BW


def ecg_filters_V5_01(lead_data):
    """
    ECG Filtering according to IEC 60601-2-25
    - Baseline wander removal
    - 50 Hz notch
    - 100 Hz notch
    - 45 Hz low-pass
    - Optional Savitzky-Golay smoothing

    NOTE: replaces the previous hand-rolled powerline-notch coefficients
    (applied via signal.lfilter) with signal.iirnotch + filtfilt. This
    is functionally the same specification (50Hz/100Hz notch, 45Hz
    low-pass, same ~1Hz baseline high-pass cutoff) but zero-phase
    throughout, instead of the mix of filtfilt (baseline) + lfilter
    (notch/low-pass) the previous version used. lfilter introduces a
    real group delay; filtfilt does not. This also reduces the phase
    offset this signal previously had relative to other pass-1
    computations that assumed lead_data timing was preserved.
    """

    lead_data = np.asarray(lead_data, dtype=float).copy()

    # Replace missing (zero) samples
    for i in range(1, len(lead_data)):
        if lead_data[i] == 0:
            lead_data[i] = lead_data[i - 1]

    Fs = 1000.0

    # ------------------------------------------------------------------
    # Baseline Wander Removal (High-pass FIR)
    # ------------------------------------------------------------------
    hp_coeff = signal.firwin(
        numtaps=2377,
        cutoff=1,
        fs=Fs,
        pass_zero=False,
        window="hamming"
    )

    baseline_removed = signal.filtfilt(hp_coeff, [1.0], lead_data)

    # ------------------------------------------------------------------
    # 50 Hz Notch Filter
    # ------------------------------------------------------------------
    notch50_b, notch50_a = signal.iirnotch(
        w0=50,
        Q=30,
        fs=Fs
    )

    filtered = signal.filtfilt(notch50_b, notch50_a, baseline_removed)

    # ------------------------------------------------------------------
    # 100 Hz Notch Filter
    # ------------------------------------------------------------------
    notch100_b, notch100_a = signal.iirnotch(
        w0=100,
        Q=30,
        fs=Fs
    )

    filtered = signal.filtfilt(notch100_b, notch100_a, filtered)

    # ------------------------------------------------------------------
    # 45 Hz Low-pass Butterworth
    # ------------------------------------------------------------------
    lp_b, lp_a = signal.butter(
        N=4,
        Wn=45,
        btype='low',
        fs=Fs
    )

    filtered = signal.filtfilt(lp_b, lp_a, filtered)

    # ------------------------------------------------------------------
    # Optional smoothing
    # ------------------------------------------------------------------
    filtered = signal.savgol_filter(
        filtered,
        window_length=29,
        polyorder=3
    )

    return filtered


def phasor_transform(signal_in, Rv):
    PT = np.empty_like(signal_in, dtype=float)
    for i in range(len(signal_in)):
        ch = signal_in[i] / Rv
        PT[i] = math.degrees(math.atan(ch))
        # PT[i] = Rv+ ()
    return (PT)


def find_PVC(signal_in, Rpeaks, Ramplitued, RRinterval):
    ##   LIST OF AREA UNDER THE CURVE
    AUC = collections.deque()
    n_sig = len(signal_in)
    for k in Rpeaks:
        # Guard against negative slice start wrapping around to the END
        # of the array (Python silently allows signal_in[-5:10], which
        # silently corrupts the AUC/PVC score for early beats instead
        # of raising an error).
        lo = max(0, k - 80)
        hi = min(n_sig, k + 80)
        ch = abs(np.trapz(signal_in[lo:hi]));
        AUC.append(ch)
        # print("median : ",ch, 1.3*st.median(AUC))
    AUC = np.array(AUC);  # print(len(AUC),AUC)
    ##    ECTOPIC FUNCTION
    PVC_list = np.empty_like(Rpeaks, dtype=float);
    PVC_list[:] = np.nan
    SVC_list = np.empty_like(Rpeaks, dtype=float);
    SVC_list[:] = np.nan
    for i in range(1, len(RRinterval)):
        if RRinterval[i] >= (1.5 * RRinterval[i - 1]):
            if Ramplitued[i] >= (Ramplitued[i - 1] + (0.2 * Ramplitued[i - 1])):  # PVC
                # Guard: at i == 1, AUC[0:i-1] == AUC[0:0] == empty,
                # and statistics.median() raises StatisticsError on an
                # empty sequence. There's no preceding-beat baseline to
                # compare against yet, so skip the PVC check for the
                # very first comparable beat instead of crashing the
                # whole buffer's analysis.
                if i > 1 and AUC[i] > (1.3 * st.median(AUC[0:i - 1])):
                    PVC_list[i] = Rpeaks[i]
            else:  # SVC or PVC
                if i > 1:
                    if AUC[i] > (1.3 * st.median(AUC[0:i - 1])):
                        PVC_list[i] = Rpeaks[i]
    return np.array(PVC_list)


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


def calculate_p_offset_p_duration_and_average_pr(p_loc, r_peak_ecg_signal, qrs_onset,
                                                 window_size=60):
    p_offset = []
    p_duration = []
    pr_differences = []

    for n in range(len(p_loc)):
        loc2 = p_loc[n] + window_size
        p_start = r_peak_ecg_signal[loc2]
        p_off = r_peak_ecg_signal[loc2 + 1]
        count = 1

        while p_start < p_off:
            p_start = r_peak_ecg_signal[loc2 + count]
            p_off = r_peak_ecg_signal[loc2 + 1 + count]
            count = count + 1

        p_offset.append(loc2 + count)
        p_duration.append(qrs_onset[n] - (p_loc[n] - count))
        pr_differences.append(qrs_onset[n] - (loc2 + count))

    average_p_duration = np.mean(np.subtract(p_offset, p_loc))
    average_pr = np.mean(pr_differences) + 80
    if average_pr < 80:
        average_pr = average_pr + 100
    if average_pr < 0:
        average_pr = average_pr + 150

    return p_offset, p_duration, average_pr


def detect_qrs_onset_offset(r_location, r_peak_ecg_signal, onset_window=50,
                            offset_window=50):  ### onset offset change into 80 80 when bundle brunch block
    qrs_onset = []
    qrs_offset = []
    signal_len = len(r_peak_ecg_signal)

    for m in range(len(r_location)):
        # QRS Onset
        loc = r_location[m] - onset_window

        if loc <= 0:
            # Guard: negative loc would silently wrap around and index
            # from the END of the array instead of erroring, corrupting
            # this beat (and anything downstream, e.g. Mode_Value).
            qrs_onset.append(0)
        else:
            R_peak = r_peak_ecg_signal[loc]
            R_prev = r_peak_ecg_signal[loc - 1]
            count = 1

            while R_peak > R_prev and (loc - count - 1) >= 0:
                R_peak = r_peak_ecg_signal[loc - count]
                R_prev = r_peak_ecg_signal[loc - count - 1]
                count = count + 1

            qrs_onset.append(max(0, loc - count))

        # QRS Offset
        loc2 = r_location[m] + offset_window

        if loc2 >= signal_len - 1:
            qrs_offset.append(signal_len - 1)
        else:
            R_on = r_peak_ecg_signal[loc2]
            R_end = r_peak_ecg_signal[loc2 + 1]
            count = 1

            while R_on < R_end and (loc2 + count + 1) < signal_len:
                R_on = r_peak_ecg_signal[loc2 + count]
                R_end = r_peak_ecg_signal[loc2 + 1 + count]
                count = count + 1

            qrs_offset.append(min(signal_len - 1, loc2 + count))

    return qrs_onset, qrs_offset


def calculate_pr_interval(r_location, qrs_onset, ecg_signal, offset_value=80):
    r_loc_new = np.empty(len(r_location), dtype=int)
    pr_array = np.empty(len(r_location))

    for v in range(len(r_location)):
        r_loc_new[v] = r_location[v] - offset_value
        start = max(0, r_loc_new[v])
        end = qrs_onset[v]
        # Guard (fix for "Mean of empty slice" seen on real device data):
        # when this beat's (R_peak - offset_value) lands at or past its
        # own QRS onset - which happens on beats with a short PR interval,
        # since offset_value=80 is a fixed guess, not scaled to that
        # beat's actual PR - the slice is empty and np.mean() on it
        # silently returns NaN (with a RuntimeWarning) instead of
        # erroring. That NaN then flows into calculate_mode()'s input.
        # Fall back to a small window immediately before QRS onset
        # instead, so this beat still contributes a real number.
        if start >= end:
            start = max(0, end - 10)
        if start >= end:
            # Still degenerate (QRS onset itself is essentially at
            # sample 0) - nothing meaningful to average; use the single
            # available sample rather than propagate NaN.
            pr_array[v] = ecg_signal[end] if 0 <= end < len(ecg_signal) else 0.0
        else:
            pr_array[v] = np.mean(ecg_signal[start:end])

    return r_loc_new, pr_array


def calculate_mode(pr_array):
    mode_value = st.mode(pr_array)
    return mode_value


def calculate_new_qrs_onset_offset(r_location, r_peak_ecg_signal, mode_value, offset_before=50,
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
            while R_peak > R_prev > mode_value and (loc - count - 1) >= 0:  # Added boundary check
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
                while r_on < r_end < mode_value and (
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
    # NOTE: an earlier revision of this file switched this to filtfilt
    # (zero-phase) to eliminate a confirmed, precisely-measured 10-sample
    # delay between the detected R-peak and the true peak in the raw
    # signal - which is real, and does make R_Location itself more
    # accurate. However, testing against real patient data showed this
    # has a serious side effect: the QRS-onset/H-onset search windows
    # elsewhere in this file (onset_window, the H-onset search bounds,
    # etc.) all anchor off R_Location, and they appear to have been
    # implicitly calibrated assuming this exact ~10-sample delay was
    # present - removing it caused H-onset detection (AH/HV) to fail on
    # 2 of 3 real recordings that previously succeeded. Reverted to
    # lfilter here; the R-peak's VISUAL marker position is instead
    # corrected only at the graph-drawing stage in analyzelead4(),
    # which doesn't touch anything the actual measurements depend on.
    data_F = signal.lfilter(h, g, filt_BW)
    data_D = np.diff(data_F);
    data_S = data_D * data_D
    # Robust whole-buffer threshold instead of a hardcoded [1000:5000]
    # window. The old fixed window assumed the first ~4 seconds of the
    # buffer were always representative of QRS amplitude - if that
    # window happened to contain noise/motion artifact (or was unusually
    # quiet) while the rest of the buffer was clean, the threshold was
    # miscalibrated for the ENTIRE buffer. A high percentile over the
    # whole signal is robust to a few extreme edge-filter-transient
    # samples while still scaling with genuine QRS amplitude, and works
    # regardless of buffer length (1s, 15s, or otherwise).
    robust_peak_ref = np.percentile(data_S, 99.5)
    peaks, rpeak = signal.find_peaks(data_S, distance=255, height=(robust_peak_ref / 1.7));
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
        # NOTE: "len(max_peak) < 2" also matches ZERO peaks found, and
        # max_peak[0] then crashes with IndexError on an empty array.
        # find_peaks() requires a true local max with lower neighbors on
        # both sides, so a peak sitting right at the edge of this window
        # (or a flat/noisy segment) can legitimately return zero peaks.
        # Only trust max_peak[0] when exactly one peak was found; zero
        # or multiple peaks both fall back to a plain argmax of the
        # segment, same as the pre-existing multi-peak fallback below.
        if len(max_peak) == 1:
            peaks_u = start_win + max_peak[0]
        else:
            peaks_u = start_win + np.argmax(seg)
        peaks_up.append(peaks_u);

        seg_s = seg * (-1);
        max_peak, peak_amp = signal.find_peaks(seg_s, distance=10)  # , height = (max(seg_s)/2))
        if len(max_peak) == 1:
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


def P_Detection(rawECG, R_peaks, RRint, Ramp):
    Filt_ECG = ecg_filters_V5_smooth(rawECG)
    peaks = R_peaks;

    PVC = find_PVC(Filt_ECG, peaks, Ramp, RRint)
    PT_pwave = phasor_transform(ecg_filters_Pwave(rawECG), Rv=0.05)
    P_st = [];
    P_sp = [];
    loc = [];
    Pamp = []
    P_location = [];
    Pst = [];
    Psp = []
    for k in range(0, len(peaks)):
        if k == 0:  # 1st P-peak detection
            win_1 = len(Filt_ECG[0:peaks[0]]);  # print(win_1)
            if win_1 > 300:
                win_st = peaks[0] - 300;
                win_sp = peaks[0] - 80
                ch = np.max(PT_pwave[peaks[0] - 300:peaks[0] - 80]);
            elif win_1 <= 300 and win_1 >= 80:
                win_st = 0;
                win_sp = peaks[0] - 80
                ch = np.max(PT_pwave[0:peaks[0] - 80]);
            elif win_1 < 80:
                # Not enough samples before this beat's own R-peak to
                # search for a P-wave at all (win_1 = samples available
                # before peaks[0]).
                #
                # BUG FIX: the original code did `break` here, which
                # exits the ENTIRE k-loop immediately - not just this
                # one beat - leaving loc/Pamp/P_st/P_sp empty (or one
                # entry short) for EVERY beat in the buffer, even beats
                # with plenty of margin. That made "Pamp[m]" (indexed by
                # beat position, later in this function) raise
                # IndexError: list index out of range on real device
                # data whenever the very first R-peak happened to fall
                # within 80 samples of the buffer start.
                #
                # Fix: append a placeholder using this beat's own
                # R-peak position, then continue to the next beat,
                # instead of skipping the append entirely (which would
                # shift every later beat's index by one relative to
                # R_Location/T_Offset elsewhere in the pipeline - the
                # same class of misalignment bug fixed earlier for the
                # two-pass R-peak detection). Using peaks[k] itself as
                # the placeholder guarantees this beat will fail the
                # existing "p_on_i >= QRS_on_i" check in
                # _evaluate_candidate_beat (a P-onset can't legitimately
                # equal the R-peak itself), so it's automatically and
                # safely skipped by the existing retry-next-candidate
                # logic instead of ever being measured or crashing.
                win_st = peaks[0]
                win_sp = peaks[0]
                loc.append(peaks[0]);
                Pamp.append(0.0)
                P_st.append(win_st);
                P_sp.append(win_sp)
                continue
        elif k > 0 and k < len(peaks) - 1:  # Second to Last but 1 peak
            win_st = int(peaks[k] - (0.4 * RRint[k]));
            win_sp = int(peaks[k] - (0.05 * (RRint[k] - 100)));  # print(win_st,win_sp)
            ch = np.max(PT_pwave[win_st:win_sp]);
        else:  # Last peak
            win_st = int(peaks[k] - (0.4 * RRint[k - 1]));
            win_sp = int(peaks[k] - (0.05 * (RRint[k - 1] - 100)));  # print(win_st,win_sp)
            ch = np.max(PT_pwave[win_st:win_sp]);
        loc_ch = (np.where(PT_pwave == ch));
        loc_ch = loc_ch[0];
        if loc_ch[0] < 80:
            loc_st = loc_ch[0]
        else:
            loc_st = loc_ch[0] - 80
        ix = np.max(Filt_ECG[loc_st:loc_ch[0] + 80]);
        loc_ch = (np.where(Filt_ECG == ix));
        loc_ch = loc_ch[0];
        loc.append(loc_ch[0]);
        Pamp.append(np.abs(Filt_ECG[loc_ch[0]]))
        P_st.append(win_st);
        P_sp.append(win_sp)

    # print("Pamp temp :", len(Pamp), Pamp);  print("Ploc temp :", len(loc), loc)

    # NOTE: a loop used to sit here:
    #   for m in range(0, len(RRint)):
    #       if np.isnan(PVC[m]) == True and Pamp[m] > (0.5 * Filt_ECG[peaks[m]]):
    #           P_location.append(loc[m]); Pst.append(P_st[m]); Psp.append(P_sp[m])
    # It has been removed: its ONLY output (appending to P_location,
    # Pst, Psp) was either immediately discarded (P_location gets
    # unconditionally overwritten by "P_location = loc" right below,
    # regardless of what this loop did) or never read again anywhere in
    # this function (Pst/Psp are local and not part of the return
    # value). Its only real-world effect was risk: "Pamp[m]" assumes
    # Pamp has exactly one entry per RR-interval, in the same order -
    # any mismatch between Pamp/loc's length (built above) and
    # RRint/PVC's length raises IndexError: list index out of range,
    # which is exactly the crash this fixes. Removing this dead code
    # eliminates that entire class of risk rather than just the one
    # trigger case (the win_1<80 branch) that happened to surface it.

    P_location = loc
    P_onset = [];  # print("P location : ",P_location)
    # print("Pst :", Pst); print("Psp :",Psp)
    #
    # Safety cap + bounds guard (fix for occasional 400+ / erratically
    # fluctuating PR interval):
    # The original loop below had NO upper bound on how far back it
    # could search, and no check that (loc - 20 - Count - 1) stays >= 0.
    # It walks backward one sample at a time, comparing the signal to
    # itself lagged by 21 samples, and only stops once that lag-21
    # comparison naturally reverses. On a real beat with baseline noise,
    # drift, or atypical P-wave morphology, that reversal can be delayed
    # for a long, noise-dependent, highly variable number of samples -
    # or (for a beat whose P-location is close to sample 0) the index
    # can go negative and silently wrap around to read garbage from the
    # END of the buffer via Python's negative indexing, rather than
    # erroring. Either way, P_onset ends up far from the true P-onset,
    # and since PR_ms = QRS_onset - P_onset, that directly produces an
    # abnormally large or wildly inconsistent PR reading. A P-onset more
    # than ~150ms before its own P-peak is not physiologically
    # plausible, so that's used as a hard cap; the array-bounds check
    # prevents the wraparound case entirely.
    MAX_P_ONSET_SEARCH = 150  # samples (~150ms at 1000Hz)
    for m in range(len(P_location)):
        loc = P_location[m]
        Count = 1
        if (loc - 20 - 1) < 0:
            # Not even enough samples before this P-location to run the
            # very first lag-21 comparison safely - fall back to a
            # small fixed offset instead of reading a negative index.
            P_onset.append(max(0, loc - 20))
            continue
        T_on = Filt_ECG[loc - 0]
        prev = Filt_ECG[loc - 20 - 1]
        while T_on > prev:
            next_idx_a = loc - 0 - Count
            next_idx_b = loc - 20 - Count - 1
            if next_idx_b < 0 or Count >= MAX_P_ONSET_SEARCH:
                # Hit the array boundary or the search cap without the
                # signal naturally reversing - stop here rather than
                # wrapping around or running away further.
                break
            T_on = Filt_ECG[next_idx_a]
            prev = Filt_ECG[next_idx_b]
            Count = Count + 1
        P_onset.append(loc - Count);
    return (P_location, Pamp, P_onset)


def ecg_filters_Twave(
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
    h, g = sp.signal.butter(4, [20 / 500], btype='low', analog=False)
    j, i = sp.signal.butter(2, [15 / 500], btype='low', analog=False)
    filt_50 = sp.signal.lfilter(d, c, lead_data);  # filt_100 = sp.signal.lfilter(f, e, filt_50)
    filt_LP = sp.signal.lfilter(h, g, filt_50)
    smoothed = sp.signal.savgol_filter(filt_LP, window_length=19, polyorder=3)
    n = 41;
    baseline_wander = sp.signal.medfilt(smoothed, kernel_size=n);
    return baseline_wander


def T_Detection(raw_signal, R_peaks, RRint):
    Filt_ECG = ecg_filters_V5_smooth(raw_signal)
    T_filt = ecg_filters_Twave(raw_signal)
    PT_twave = phasor_transform(T_filt, Rv=0.1)
    T_st = [];
    T_sp = [];
    T_loc = [];
    Tamp = []
    for k in range(0, len(R_peaks)):
        if k == len(R_peaks) - 1:
            win_st = int(R_peaks[k] + (0.16 * RRint[k - 1]));
            win_sp = int(R_peaks[k] + (0.57 * (RRint[k - 1])));  # print(win_st,win_sp)
        elif k > 0 and k < len(R_peaks) - 1:
            win_st = int(R_peaks[k] + (0.16 * RRint[k]));
            win_sp = int(R_peaks[k] + (0.57 * (RRint[k])));  # print(win_st,win_sp)
        else:
            win_st = int(R_peaks[k] + (0.16 * RRint[k + 1]));
            win_sp = int(R_peaks[k] + (0.57 * (RRint[k + 1])));  # print(win_st,win_sp)
        # Check if the slice is empty and handle it.
        # win_st < win_sp as raw integers does NOT guarantee a non-empty
        # slice: if both fall past the end of the buffer (common for the
        # last beat, whose window is sized off RRint[k-1]), Python
        # silently truncates PT_twave[win_st:win_sp] to an empty array
        # instead of raising - and np.max() on an empty array throws
        # "zero-size array to reduction operation maximum which has no
        # identity", which crashed the ENTIRE buffer's analysis (the
        # exception propagates out of T_Detection -> compute_his_bundle_
        # intervals -> analyzelead4's except block -> all-zero result).
        # Clamp both bounds to the signal length first so the emptiness
        # check reflects the slice that will actually be taken.
        sig_len = len(PT_twave)
        win_st_c = max(0, min(win_st, sig_len))
        win_sp_c = max(0, min(win_sp, sig_len))
        if win_st_c >= win_sp_c:
            print(f"Warning: Empty slice for R-peak index {k}. win_st: {win_st}, win_sp: {win_sp}")
            # Skip this iteration if the slice is empty
            continue
        else:
            ch = np.max(PT_twave[win_st_c:win_sp_c])
        loc_ch = (np.where(PT_twave == ch));
        loc_ch = loc_ch[0];
        # print("window :",loc_ch[0]-80,loc_ch[0]+80)

        # Check if loc_ch is empty before indexing
        if len(loc_ch) > 0:
            # Clamp the +-80 window to valid bounds. loc_ch[0] comes from
            # np.where(PT_twave == ch), which searches the WHOLE array
            # for that value - not just the current beat's window - so
            # it can legitimately land close to sample 0 for some beats.
            # When it does, "loc_ch[0] - 80" goes negative and Python
            # silently wraps that into a slice start near the END of the
            # array (while the end of the slice is still small), making
            # Filt_ECG[start:end] empty and crashing np.max() with
            # "zero-size array to reduction operation maximum which has
            # no identity" - the exact traceback this fixes.
            ecg_len = len(Filt_ECG)
            lo = max(0, loc_ch[0] - 80)
            hi = min(ecg_len, loc_ch[0] + 80)
            if hi <= lo:
                print(f"Warning: Empty ECG window for R-peak index {k} "
                      f"(loc_ch[0]={loc_ch[0]}, buffer length {ecg_len}). Skipping.")
                continue
            ix = np.max(Filt_ECG[lo:hi]);
            loc_ch = (np.where(Filt_ECG == ix));
            loc_ch = loc_ch[0];
            if len(loc_ch) == 0:
                print(f"Warning: no matching Filt_ECG index for R-peak index {k}. Skipping.")
                continue
            T_loc.append(loc_ch[0]);
            Tamp.append(np.abs(Filt_ECG[loc_ch[0]]))
            T_st.append(win_st);
            T_sp.append(win_sp)
        else:
            print(f"Warning: loc_ch is empty for R-peak index {k}. Skipping this iteration.")
            # Handle the case where loc_ch is empty, e.g., append NaN to T_loc, etc.

    return (np.array(T_loc))


def calculate_t_offset(t_loc, r_peak_ecg_signal):
    t_offset = []
    signal_length = len(r_peak_ecg_signal)

    for o in range(len(t_loc)):
        location = t_loc[o]
        t_start = r_peak_ecg_signal[location]
        t_off = r_peak_ecg_signal[min(location + 1, signal_length - 1)]
        count = 1

        while t_start < t_off and location + count + 1 < signal_length:
            t_start = r_peak_ecg_signal[min(location + count, signal_length - 1)]
            t_off = r_peak_ecg_signal[min(location + count + 1, signal_length - 1)]
            count += 1

        # Ensure the offset doesn't go out of bounds
        final_offset = min(location + count + 100, signal_length - 1)
        t_offset.append(final_offset)

    return t_offset


def calculate_snr_db(raw_signal, filtered_signal):
    """
    Whole-buffer SNR: compares the raw (unfiltered) signal against the
    smoothed/filtered signal, treating the filtered signal as "signal"
    and whatever the filter removed (raw - filtered) as "noise".

    NOTE: this is an inferred standard implementation. The actual
    calculate_snr_db() from the Colab reference notebook was not
    provided - if it computes SNR differently, swap this out with the
    real definition.
    """
    signal_power = np.mean(filtered_signal ** 2)
    noise = raw_signal - filtered_signal
    noise_power = np.mean(noise ** 2)

    eps = 1e-12
    if noise_power > eps:
        return 10 * np.log10(max(signal_power, eps) / noise_power)
    else:
        return float("inf")


def bandpass(signal_in, fs, lowcut=30, highcut=100, order=4):
    # Matched exactly to the Colab reference implementation
    # (def bandpass(signal, fs, lowcut=30, highcut=100, order=4)).
    # A previous revision of this file used highcut=499.9, reasoning
    # that a narrower 30-100Hz band was distorting the A-wave/H-onset
    # deflections - that reasoning was never validated against the
    # actual Colab reference and contradicted it, so it has been
    # reverted back to the validated 30-100Hz band.
    nyq = 0.5 * fs
    b, a = butter(order, [lowcut / nyq, highcut / nyq], btype='band')
    return filtfilt(b, a, signal_in)


# ================================================================
#   SHARED REAL ALGORITHM
#   This is the single source of truth for PA / AH / HV / PR /
#   QRS / SNR. Both process() (console/debug) and analyzelead4()
#   (called from Android) call this so they can never drift out
#   of sync again.
# ================================================================

def compute_his_bundle_intervals(v2, fs=1000, lsb_V=286e-9):
    """
    v2: already-scaled, already-inverted lead signal in mV, i.e.
        v2 = (raw_samples * 0.000286) * (-gain)
    Returns a dict with PA, AH, HV, PR, QRS (all ms) and SNR (dB).
    Raises ValueError if a clean best-beat/segment could not be
    resolved from this buffer (e.g. buffer too short / too noisy).
    """

    ###--------------------------- Raw_data fetching and R peak detection ----------------------------###
    R_data = v2 * (-1)
    R_peak_ecg_signal = ecg_filters_V5_01(R_data)
    R_Location, ectopic, S_Location = R_Peak_Detection_05(R_data)
    R_Location = np.array(R_Location, dtype=int)

    # Keep this PASS-1 signal and R-peak positions under their own names.
    # QRS_Onset/New_QRS_Onset/P_onset below are all computed against
    # THIS signal/these positions. A second R-peak detection pass runs
    # later (on a differently-filtered signal, for T-wave localization)
    # and used to silently overwrite R_peak_ecg_signal/R_Location here -
    # but ecg_filters_V5_01 and ecg_filters_V5_smooth use lfilter
    # internally (not filtfilt), which introduces a real phase/group
    # delay. In testing this produced a consistent ~6-sample offset
    # between the "same" R-peak across the two passes. Measuring a beat
    # (_evaluate_candidate_beat) by slicing the SECOND pass's signal
    # using window boundaries computed from the FIRST pass's signal
    # silently shifts the P-onset->QRS-onset segment by that offset -
    # small in absolute terms, but large relative to the ~55-95 sample
    # H-onset search window, and a plausible dominant cause of
    # intermittently wrong/zero AH (and possibly HV) values with no
    # error ever being raised. Fix: always measure a beat using the
    # SAME signal/positions its P-onset and QRS-onset were derived from.
    R_peak_ecg_signal_meas = R_peak_ecg_signal
    R_Location_meas = R_Location

    if len(R_Location) < 3:
        raise ValueError("Not enough R peaks detected in this buffer.")

    ###--------------------------Heart Rate calculation---------------------------------- ###
    R_amp = [];
    RRint = [];
    for i in range(0, len(R_Location) - 1):
        ch = R_Location[i + 1] - R_Location[i];
        RRint.append(ch)
        R_amp.append(R_peak_ecg_signal[R_Location[i]])
        if i == (len(R_Location) - 2):
            R_amp.append(R_peak_ecg_signal[R_Location[i + 1]])

    QRS_Onset, QRS_Offset = detect_qrs_onset_offset(R_Location, R_peak_ecg_signal)
    R_Loc_New, PR_Array = calculate_pr_interval(R_Location, QRS_Onset, R_peak_ecg_signal)
    Mode_Value = calculate_mode(PR_Array)
    New_QRS_Onset, New_QRS_Offset = calculate_new_qrs_onset_offset(R_Location, R_peak_ecg_signal,
                                                                   Mode_Value)
    P_location, Pamp, P_onset = P_Detection(R_data, R_Location, RRint, R_amp)

    filtered_v2 = ecg_filters_V5_smooth(v2)
    R_data = filtered_v2 * (-1)

    # ---------- Whole-buffer SNR ----------
    # Matched to the Colab reference: compares the ORIGINAL raw signal
    # against the SMOOTHED signal across the entire buffer, not just
    # the one selected beat's tiny His-bundle window. This measures
    # overall recording quality rather than one beat's local quality.
    raw_signal = v2 * (-1)
    filtered_signal = R_data
    SNR_dB = calculate_snr_db(raw_signal, filtered_signal)

    R_peak_ecg_signal = ecg_filters_V5_smooth(R_data)
    R_Location, ectopic, S_Location = R_Peak_Detection_05(R_data)
    T_loc = T_Detection(R_peak_ecg_signal, R_Location, RRint)
    T_Offset = calculate_t_offset(T_loc, R_peak_ecg_signal)

    T_Offset = np.array(T_Offset, dtype=int)
    P_location = np.array(P_location, dtype=int)
    P_onset = np.array(P_onset, dtype=int)

    ads_values_tloc_p_location = []
    ads_seg_bounds = []  # (seg_start, seg_end) per candidate, needed for the diagnostic SNR below
    for i, t_offset in enumerate(T_Offset):
        next_p_onset = next((p for p in P_onset if p > t_offset), None)
        if next_p_onset is not None and next_p_onset < len(R_peak_ecg_signal):
            seg_start = int(t_offset)
            seg_end = int(next_p_onset)
            ads_segment = np.round(np.abs(R_peak_ecg_signal[seg_start:seg_end] / 0.000286)).astype(int)
            ads_values_tloc_p_location.append(ads_segment)
            ads_seg_bounds.append((seg_start, seg_end))

    if not ads_values_tloc_p_location:
        raise ValueError("No T→P segments found for ADS analysis.")

    # -------------------------------------------------------------------------
    #                SELECT BEST SEGMENTS (by RMS voltage)
    # -------------------------------------------------------------------------
    # Matched to the validated Colab reference implementation: ranks each
    # beat's T->P baseline by RMS amplitude, descending (highest-RMS
    # tried first). This is the actual production selection criterion.
    #
    # A per-beat SNR variant (raw vs filtered baseline, same math as the
    # whole-buffer SNR) was tried as an alternative ranking metric, but
    # reverted back to RMS as the default: RMS is what Colab was actually
    # validated against, while the SNR formula itself is an inferred
    # implementation never confirmed against the real Colab SNR
    # definition, and per-beat SNR ranking has no clinical validation of
    # its own. Per-beat SNR is still computed below and logged as a
    # DIAGNOSTIC alongside RMS, so the two can be compared against real
    # recordings over time without letting an unverified metric decide
    # which beat is actually measured.
    segment_scores = []
    segment_indices = []
    diagnostic_snr_by_index = {}

    for i, ads_segment in enumerate(ads_values_tloc_p_location):
        ads_segment_f = np.asarray(ads_segment, dtype=float)
        if len(ads_segment_f) < 10:  # ignore very small segments
            continue
        voltage = ads_segment_f * lsb_V
        rms = np.sqrt(np.mean(voltage ** 2))
        segment_scores.append(rms)
        segment_indices.append(i)

        # Diagnostic-only per-beat SNR (does not affect selection).
        seg_start, seg_end = ads_seg_bounds[i]
        raw_local = raw_signal[seg_start:seg_end]
        filt_local = filtered_signal[seg_start:seg_end]
        if len(raw_local) >= 10 and len(filt_local) >= 10:
            diagnostic_snr_by_index[i] = calculate_snr_db(raw_local, filt_local)

    segment_scores = np.array(segment_scores)
    segment_indices = np.array(segment_indices)

    if len(segment_indices) == 0:
        raise ValueError("No valid segments (all shorter than 10 samples) for ADS analysis.")

    # Sort DESCENDING: highest-RMS beat tried first (matches Colab).
    sorted_order = np.argsort(segment_scores)[::-1]
    candidate_indices = segment_indices[sorted_order]  # ALL candidates, best (highest RMS) first

    # Diagnostic printout: RMS ranking (actually used) side-by-side with
    # per-beat SNR (not used, logged for comparison only). Visible in
    # Logcat under python.stdout when running via analyzelead4 on
    # Android, and in the console table when running via process().
    print("\n--- Per-beat selection diagnostics (RMS = used, SNR = logged only) ---")
    for rank, idx in enumerate(candidate_indices):
        rms_val = segment_scores[sorted_order[rank]]
        snr_val = diagnostic_snr_by_index.get(int(idx), float("nan"))
        marker = "  <-- SELECTED (if it passes validation)" if rank == 0 else ""
        print(f"  rank {rank}: beat #{idx}  RMS={rms_val:.6f} V  SNR={snr_val:.2f} dB{marker}")
    print("--- end diagnostics ---\n")

    # Try every candidate beat, best (quietest baseline) first, and use
    # the first one that actually passes validation. The old code only
    # ever tried the single top-ranked candidate and raised/crashed the
    # ENTIRE buffer's analysis if that one beat happened to fail a
    # validity check (e.g. "P-onset is not before QRS-onset") - even
    # though 4+ other perfectly usable candidate beats had already been
    # computed and were sitting right there unused.
    last_error = None
    result_core = None

    for candidate_idx in candidate_indices:
        try:
            result_core = _evaluate_candidate_beat(
                candidate_idx,
                P_onset=P_onset,
                R_Location=R_Location_meas,
                New_QRS_Onset=New_QRS_Onset,
                New_QRS_Offset=New_QRS_Offset,
                R_peak_ecg_signal=R_peak_ecg_signal_meas,
                fs=fs,
            )
            break
        except ValueError as e:
            last_error = e
            continue

    if result_core is None:
        raise ValueError(
            f"No usable beat found among {len(candidate_indices)} candidates "
            f"(last error: {last_error})."
        )

    # Apply the whole-buffer SNR (matched to Colab) computed earlier,
    # rather than a per-beat value - _evaluate_candidate_beat leaves
    # "SNR" as None on purpose since this is the single source of truth.
    result_core["SNR"] = float(SNR_dB)

    return result_core


def _evaluate_candidate_beat(
        candidate_idx,
        P_onset,
        R_Location,
        New_QRS_Onset,
        New_QRS_Offset,
        R_peak_ecg_signal,
        fs,
):
    """
    Attempt to compute PA/AH/HV/PR/QRS/SNR for ONE candidate beat.
    Raises ValueError if this particular beat doesn't pass validation
    (caller is expected to try the next-best candidate in that case).
    """

    if candidate_idx >= len(P_onset) or candidate_idx >= len(R_Location):
        raise ValueError("Candidate segment index out of range for P_onset / R_Location.")

    R_peak_i = int(R_Location[candidate_idx])

    # ---------- QRS onset ----------
    qrs_candidates = [q for q in New_QRS_Onset if q < R_peak_i]
    if len(qrs_candidates) == 0:
        raise ValueError("No QRS onset found before this candidate's R peak.")
    QRS_on_i = qrs_candidates[-1]

    # ---------- QRS offset ----------
    qrs_off_candidates = [q for q in New_QRS_Offset if q > R_peak_i]
    if len(qrs_off_candidates) == 0:
        raise ValueError("No QRS offset found after this candidate's R peak.")
    QRS_off_i = qrs_off_candidates[0]

    # ---------- P onset for this beat ----------
    p_on_i = int(P_onset[candidate_idx])
    if p_on_i >= QRS_on_i:
        raise ValueError("P-onset is not before QRS-onset for this candidate beat.")

    # ---------- Bandpass the P-onset -> QRS-onset segment ----------
    seg_start = p_on_i
    seg_end = QRS_on_i + 1
    raw_segment = R_peak_ecg_signal[seg_start:seg_end]

    if len(raw_segment) <= 10:
        raise ValueError("P-onset to QRS-onset segment too short to bandpass filter.")

    bp = bandpass(raw_segment, fs)

    # ---------- Scaled bandpassed trace for graphing ----------
    # Matches Colab's bp_scaled: normalize the bandpassed (His-Purkinje)
    # component to +-1 and rescale it to roughly the same visual
    # amplitude as the raw segment, purely so it can be overlaid on the
    # raw trace and stay visible instead of looking flat next to the
    # much larger QRS/P-wave amplitudes.
    bp_abs_max = np.max(np.abs(bp))
    if bp_abs_max > 0:
        bp_norm = bp / bp_abs_max
        scale = 0.5 * np.max(np.abs(raw_segment))
        bp_scaled = bp_norm * scale
    else:
        bp_scaled = bp

    # ---------- A wave (true peak in early PR region) ----------
    qrs_local = QRS_on_i - seg_start
    search_end = int(0.45 * qrs_local)
    sub = bp[15:search_end]
    if len(sub) == 0:
        raise ValueError("A-wave search window is empty for this candidate beat.")
    peak_idx = np.argmax(sub)
    A_wave_i = seg_start + 15 + peak_idx

    # -------------------------------------------------------
    #        H_on and H_off DETECTION (TRUE BIPHASIC START)
    # -------------------------------------------------------
    H_on_i = None
    bp_segment = bp  # bandpassed signal

    # search region before QRS (where the circled wave is)
    search_start = max(0, qrs_local - 60)
    search_end = qrs_local - 5
    sub = bp_segment[search_start:search_end]

    # Step 1: find zero crossing (center of biphasic)
    zc_idx = None
    for i in range(1, len(sub)):
        if sub[i - 1] < 0 and sub[i] > 0:
            zc_idx = i
            break

    # Step 2: go BACK to find beginning of that wave.
    # Matched exactly to the Colab reference's fixed 0.001 flatness
    # threshold. A previous revision of this file made this adaptive
    # (scaled to each beat's own amplitude) to reduce how often H_on_i
    # went undetected - but that deviated from the validated Colab
    # implementation without device data to justify it. Reverted to
    # match Colab exactly; see the fallback behavior below for the
    # correct way Colab itself handles a genuine non-detection.
    if zc_idx is not None:
        for i in range(zc_idx, 1, -1):
            # beginning = where slope starts increasing
            if abs(sub[i] - sub[i - 1]) < 0.001:
                H_on_i = seg_start + search_start + i
                break

    # search between H_on and QRS
    H_off_i = None
    if H_on_i is not None:
        h_on_local = H_on_i - seg_start

        search_start = h_on_local + 5
        search_end = qrs_local - 2

        sub = bp_segment[search_start:search_end]

        if len(sub) > 5:
            # find LAST zero-crossing before QRS
            for i in range(len(sub) - 1, 1, -1):
                if sub[i - 1] > 0 and sub[i] < 0:
                    H_off_i = seg_start + search_start + i
                    break

    # ---------- Intervals ----------
    # Matched exactly to the Colab reference: if H_on_i was not found,
    # AH_ms/HV_ms are NaN (genuinely "not measured"), NOT a forced
    # substitute value. A previous revision of this file forced
    # H_on_i = A_wave_i as a fallback, which made AH_ms silently read
    # exactly 0.0 (a real-looking but fake number) while HV_ms silently
    # absorbed the true AH duration - indistinguishable from a real
    # zero-length AH interval. Colab's own table-building code reports
    # NaN in this situation instead, which is the honest signal that
    # detection failed for this beat rather than a false measurement.
    PA_ms = A_wave_i - p_on_i
    if H_on_i is not None:
        AH_ms = H_on_i - A_wave_i
        HV_ms = QRS_on_i - H_on_i
    else:
        AH_ms = float("nan")
        HV_ms = float("nan")
    PR_ms = QRS_on_i - p_on_i
    QRS_ms = QRS_off_i - QRS_on_i

    return {
        "PA": float(PA_ms),
        "AH": float(AH_ms),
        "HV": float(HV_ms),
        "PR": float(PR_ms),
        "QRS": float(QRS_ms),
        # SNR is intentionally NOT set here - compute_his_bundle_intervals
        # overrides it with the whole-buffer SNR (matching Colab), since
        # per-beat SNR was superseded by that whole-buffer measurement.
        "SNR": None,
        # Landmark sample indices for the beat that was actually used,
        # so a caller can plot exactly what was measured instead of
        # just the raw trace. Not part of the original 6-value result,
        # purely additive - existing callers that only read
        # PA/AH/HV/PR/QRS/SNR are unaffected. H_on_i/H_off_i may be
        # None here if detection genuinely failed for this beat - the
        # graph code must guard against that (see analyzelead4 below).
        "R_peak_i": R_peak_i,
        "p_on_i": p_on_i,
        "A_wave_i": A_wave_i,
        "H_on_i": H_on_i,
        "H_off_i": H_off_i,
        "QRS_on_i": QRS_on_i,
        "QRS_off_i": QRS_off_i,
        "seg_start": seg_start,
        "seg_end": seg_end,
        "bp_scaled": bp_scaled,
    }


def analyzelead4(samples, graph_path):
    """
    Analyze Lead4 ECG data and return ECG intervals.
    Returns: [PA, AH, HV, PR, QRS, SNR, graph_path]
    (HH has been removed — it is no longer computed or returned.)
    This function is called from Android OfflineProcessor.
    """
    try:
        import matplotlib
        matplotlib.use("Agg")
        import matplotlib.pyplot as plt

        fs = 1000

        # ----------------------------------
        # Convert Java ArrayList to Python list
        # ----------------------------------
        try:
            # Direct conversion from Java ArrayList to NumPy (faster)
            size = samples.size()
            lead4 = np.zeros(size, dtype=np.float64)
            for i in range(size):
                lead4[i] = float(samples.get(i))
        except Exception:
            # Already Python list
            lead4 = np.array([float(x) for x in samples], dtype=np.float64)

        # Quick validation
        if len(lead4) == 0:
            return [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ""]

        # ----------------------------------
        # Convert Lead4 to V2 (mV)
        # ----------------------------------
        gain = 1
        scale = 0.000286
        v2 = ((lead4) * scale) * (-(gain))

        # ----------------------------------
        # Real interval detection (same algorithm process() uses)
        # ----------------------------------
        result = compute_his_bundle_intervals(v2, fs=fs)

        # ----------------------------------
        # Generate Graph
        # ----------------------------------
        # Two panels: (1) the full raw trace for context, and (2) a
        # zoomed-in view of the actual beat that was measured, with the
        # detected landmarks marked. The old graph only showed panel 1,
        # which meant there was no way to visually confirm WHICH beat
        # was used or WHERE the PA/AH/HV/PR/QRS boundaries were placed.
        fig, axes = plt.subplots(2, 1, figsize=(12, 9))

        axes[0].plot(v2, color='blue', linewidth=0.8)
        axes[0].set_title('Full ECG Trace (Lead4 / V2)')
        axes[0].set_xlabel('Sample')
        axes[0].set_ylabel('Amplitude (mV)')
        axes[0].grid(True)

        margin = 150
        zoom_start = max(0, result["seg_start"] - margin)
        zoom_end = min(len(v2), result["QRS_off_i"] + margin)

        if zoom_end > zoom_start:
            x_zoom = np.arange(zoom_start, zoom_end)
            axes[1].plot(x_zoom, v2[zoom_start:zoom_end], color='black', linewidth=1.0,
                         label='Raw beat')

            # Overlay the bandpassed His-Purkinje component (Colab's
            # bp_scaled), aligned to its own P-onset -> QRS-onset x-range
            # rather than the wider zoom window, and scaled for
            # visibility against the much larger QRS/P amplitudes.
            bp_scaled = result.get("bp_scaled")
            if bp_scaled is not None and len(bp_scaled) > 0:
                x_bp = np.arange(result["seg_start"], result["seg_start"] + len(bp_scaled))
                axes[1].plot(x_bp, bp_scaled, color='teal', linewidth=1.2, linestyle='-',
                             alpha=0.85, label='Bandpassed His signal (scaled)')

            # Display-only R-peak correction: R_peak_i itself (used for
            # every internal calculation - QRS onset/offset windows,
            # RRint, etc.) is intentionally left untouched, since
            # testing showed those internal windows are implicitly
            # calibrated around R_Peak_Detection_05's known ~10-sample
            # delay, and "fixing" it internally broke H-onset detection
            # on real data. But the marker as originally drawn was
            # plotted at that same delayed position against the RAW,
            # undelayed v2 trace - visibly off from the actual peak tip.
            # This looks for the true local maximum in the raw signal
            # within a small window around the reported position, purely
            # for where the line gets drawn on this graph. Self-scaling
            # (searches for whatever the true local max actually is,
            # rather than assuming a fixed sample-count correction), so
            # it stays correct even if the underlying delay amount ever
            # changes.
            r_peak_display = result["R_peak_i"]
            search_lo = max(0, r_peak_display - 20)
            search_hi = min(len(v2), r_peak_display + 20)
            if search_hi > search_lo:
                local_window = v2[search_lo:search_hi]
                # Search for the POSITIVE peak specifically (the R-wave
                # convention this codebase already uses elsewhere -
                # R_Peak_Detection_05 explicitly separates "peaks_up"
                # (positive) from "peaks_up_s" (negative/S-wave) via two
                # separate searches). Using argmax(abs(...)) here instead
                # would risk snapping onto a nearby, larger-magnitude
                # negative S-wave dip rather than the true R-wave peak -
                # confirmed happening on real patient data during
                # testing (a -9.64 S-wave dip was picked over the true
                # +8.08 R-wave peak a few samples away) before this fix.
                r_peak_display = search_lo + int(np.argmax(local_window))

            landmarks = [
                ('P-onset', result["p_on_i"], 'green'),
                ('A-wave', result["A_wave_i"], 'orange'),
                ('H-onset', result["H_on_i"], 'purple'),
                ('H-offset', result["H_off_i"], 'brown'),
                ('QRS-onset', result["QRS_on_i"], 'red'),
                ('QRS-offset', result["QRS_off_i"], 'red'),
                ('R-peak', r_peak_display, 'blue'),
            ]

            y_top = np.max(v2[zoom_start:zoom_end])
            for label, idx, color in landmarks:
                # idx can be None now if H-onset/H-offset detection
                # genuinely failed for this beat (AH/HV reported as NaN
                # to match Colab) - skip plotting that landmark instead
                # of crashing on "None < int".
                if idx is not None and zoom_start <= idx < zoom_end:
                    axes[1].axvline(idx, color=color, linestyle='--', linewidth=1)
                    axes[1].text(idx, y_top, label, rotation=90,
                                 va='top', ha='right', fontsize=8, color=color)

            axes[1].legend(loc='lower right', fontsize=8)

        axes[1].set_title('Best His Bundle Segment (Detected Landmarks)')
        axes[1].set_xlabel('Sample')
        axes[1].set_ylabel('Amplitude (mV)')
        axes[1].grid(True)

        plt.tight_layout()
        plt.savefig(graph_path, dpi=150, bbox_inches='tight')
        plt.close()

        # ----------------------------------
        # Return: [PA, AH, HV, PR, QRS, SNR, graph_path]
        # ----------------------------------
        return [
            result["PA"],
            result["AH"],
            result["HV"],
            result["PR"],
            result["QRS"],
            result["SNR"],
            str(graph_path)
        ]

    except Exception as e:
        import traceback
        print("================================")
        print("PYTHON ERROR in analyzelead4")
        print(str(e))
        traceback.print_exc()
        print("================================")
        return [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, ""]


def process(buffer2):
    """
    Console/debug entry point. Uses the exact same real algorithm
    as analyzelead4() via compute_his_bundle_intervals(), then
    prints a formatted table.
    """
    fs = 1000

    v2 = np.array(buffer2, dtype=np.float64)
    gain = 1
    scale = 0.000286
    # Convert to mV and invert
    v2 = ((v2) * scale) * (-(gain));

    result = compute_his_bundle_intervals(v2, fs=fs)

    PA_ms = result["PA"]
    AH_ms = result["AH"]
    HV_ms = result["HV"]
    PR_ms = result["PR"]
    QRS_ms = result["QRS"]
    SNR_dB = result["SNR"]

    ### Display #####
    df = pd.DataFrame(
        [[int(PA_ms), int(AH_ms), int(HV_ms), int(PR_ms), int(QRS_ms)]],
        columns=[
            "PA (ms)",
            "AH (ms)",
            "HV (ms)",
            "PR (ms)",
            "QRS (ms)"
        ]
    )

    # ANSI escape codes
    BOLD = "\033[1m"
    BLACK = "\033[30m"
    RESET = "\033[0m"
    RED = "\033[91m"
    BLUE = "\033[94m"
    print("\n")
    print(BOLD + BLACK + "=" * 60 + RESET)
    print(BOLD + BLACK + "           BEST  HIS BUNDLE SEGMENT" + RESET)
    print(BOLD + BLACK + "=" * 60 + RESET)
    print()
    print(BOLD + df.to_string(index=False) + RESET)
    print(BOLD + BLACK + "=" * 60 + RESET)
    print()

    snr_color = RED if SNR_dB < 6 else BLUE
    print(f"{BOLD}{snr_color}SNR of Best Segment : {SNR_dB:.2f} dB{RESET}")
    print(BOLD + BLACK + "=" * 60 + RESET)

    return {
        "PA": PA_ms,
        "AH": AH_ms,
        "HV": HV_ms,
        "PR": PR_ms,
        "QRS": QRS_ms,
        "SNR": SNR_dB
    }
