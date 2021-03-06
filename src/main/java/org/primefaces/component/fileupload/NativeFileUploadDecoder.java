/**
 * Copyright 2009-2019 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.component.fileupload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.NativeUploadedFile;
import org.primefaces.model.UploadedFileWrapper;
import org.primefaces.util.FileUploadUtils;
import org.primefaces.virusscan.VirusException;

public class NativeFileUploadDecoder {

    private NativeFileUploadDecoder() {
    }

    public static void decode(FacesContext context, FileUpload fileUpload, String inputToDecodeId) {
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        try {
            if (fileUpload.getMode().equals("simple")) {
                decodeSimple(context, fileUpload, request, inputToDecodeId);
            }
            else {
                decodeAdvanced(context, fileUpload, request);
            }
        }
        catch (IOException ioe) {
            throw new FacesException(ioe);
        }
        catch (ServletException se) {
            throw new FacesException(se);
        }
    }

    private static void decodeSimple(FacesContext context, FileUpload fileUpload, HttpServletRequest request, String inputToDecodeId)
            throws IOException, ServletException {

        if (fileUpload.isMultiple()) {
            Iterable<Part> parts = request.getParts();
            List<Part> uploadedInputParts = new ArrayList<>();

            Iterator<Part> iterator = parts.iterator();
            while (iterator.hasNext()) {
                Part p = iterator.next();

                if (p.getName().equals(inputToDecodeId)) {
                    uploadedInputParts.add(p);
                }
            }

            if (!uploadedInputParts.isEmpty() && isValidFile(context, fileUpload, uploadedInputParts)) {
                fileUpload.setSubmittedValue(new UploadedFileWrapper(new NativeUploadedFile(uploadedInputParts, fileUpload)));
            }
            else {
                fileUpload.setSubmittedValue("");
            }
        }
        else {
            Part part = request.getPart(inputToDecodeId);

            if (part != null) {
                NativeUploadedFile uploadedFile = new NativeUploadedFile(part, fileUpload);
                if (isValidFile(context, fileUpload, uploadedFile)) {
                    fileUpload.setSubmittedValue(new UploadedFileWrapper(uploadedFile));
                }
            }
            else {
                fileUpload.setSubmittedValue("");
            }
        }
    }

    private static void decodeAdvanced(FacesContext context, FileUpload fileUpload, HttpServletRequest request) throws IOException, ServletException {
        String clientId = fileUpload.getClientId(context);
        Part part = request.getPart(clientId);

        if (part != null) {
            NativeUploadedFile uploadedFile = new NativeUploadedFile(part, fileUpload);
            if (isValidFile(context, fileUpload, uploadedFile)) {
                fileUpload.queueEvent(new FileUploadEvent(fileUpload, uploadedFile));
            }
        }
    }

    private static boolean isValidFile(FacesContext context, FileUpload fileUpload, NativeUploadedFile uploadedFile) throws IOException {
        boolean valid = (fileUpload.getSizeLimit() == null || uploadedFile.getSize() <= fileUpload.getSizeLimit()) && FileUploadUtils.isValidType(fileUpload,
                uploadedFile.getFileName(), uploadedFile.getInputstream());
        if (valid) {
            try {
                FileUploadUtils.performVirusScan(context, fileUpload, uploadedFile.getInputstream());
            }
            catch (VirusException ex) {
                return false;
            }
        }
        return valid;
    }

    private static boolean isValidFile(FacesContext context, FileUpload fileUpload, List<Part> parts) throws IOException {
        long totalPartSize = 0;
        for (int i = 0; i < parts.size(); i++) {
            Part p = parts.get(i);
            totalPartSize += p.getSize();
            NativeUploadedFile uploadedFile = new NativeUploadedFile(p, fileUpload);
            if (!FileUploadUtils.isValidType(fileUpload, uploadedFile.getFileName(), uploadedFile.getInputstream())) {
                return false;
            }
            try {
                FileUploadUtils.performVirusScan(context, fileUpload, uploadedFile.getInputstream());
            }
            catch (VirusException ex) {
                return false;
            }
        }

        return fileUpload.getSizeLimit() == null || totalPartSize <= fileUpload.getSizeLimit();
    }
}
