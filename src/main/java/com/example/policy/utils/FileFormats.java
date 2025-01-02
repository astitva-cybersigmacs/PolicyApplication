package com.example.policy.utils;

import java.util.*;

public class FileFormats {

	public static Set<String> certificateFormats() {
		Set<String> set = new HashSet<String>();
		set.add("image/png");
		set.add("image/jpeg");
		set.add("image/jpg");
		set.add("application/pdf");
		return set;
	}
	public static List<String> userProfilePictureFormat() {
		List<String> imagesType = new ArrayList<>();
		imagesType.add("image/jpeg");
		imagesType.add("image/jpg");
		imagesType.add("image/png");
		return imagesType;
	}
	public static Set<String> charterFileFormat() {
		Set<String> set = new HashSet<String>();
		set.add("application/pdf");
		set.add("image/jpeg");
		set.add("image/jpg");
		set.add("image/png");
		return set;
	}

	public static Set<String> proposalFileFormat() {
		Set<String> set = new HashSet<String>();
		set.add("application/pdf");
		set.add("image/jpeg");
		set.add("image/jpg");
		set.add("image/png");
		return set;
	}


    public static Set<String> accountFileFormat() {
		Set<String> set = new HashSet<String>();
		set.add("application/pdf");
		return set;
    }
}

