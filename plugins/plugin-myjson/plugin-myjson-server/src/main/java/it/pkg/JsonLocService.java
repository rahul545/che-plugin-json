/*
 * Copyright (c) 2012-2017 All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package it.pkg;

import static org.eclipse.che.api.fs.server.WsPathUtils.nameOf;

import com.google.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.WsPathUtils;

/** Service for counting lines of code within all JSON files in a given project. */
@Path("json-example/{ws-id}")
public class JsonLocService {

  private final FsManager fsManager;

  /** Constructor for the JSON Exapmle lines of code service. */
  @Inject
  public JsonLocService(FsManager fsManager) {
    this.fsManager = fsManager;
  }

  private int countLines(String wsPath)
      throws ServerException, NotFoundException, ConflictException {
    String content = fsManager.readAsString(wsPath);
    String[] lines = content.split("\r\n|\r|\n");
    return lines.length;
  }

  private boolean isJsonFile(String wsPath) {
    return nameOf(wsPath).endsWith("json");
  }

  /**
   * Count LOC for all JSON files within the given project.
   *
   * @param projectPath the path to the project that contains the JSON files for which to calculate
   *     the LOC
   * @return a Map mapping the file name to their respective LOC value
   * @throws ServerException in case the server encounters an error
   * @throws NotFoundException in case the project couldn't be found
   */
  @GET
  @Path("{projectPath}")
  public Map<String, String> countLinesPerFile(@PathParam("projectPath") String projectPath)
      throws ServerException, NotFoundException, ConflictException {
    String projectWsPath = WsPathUtils.absolutize(projectPath);

    Map<String, String> linesPerFile = new LinkedHashMap<>();
    Set<String> fileWsPaths = fsManager.getFileWsPaths(projectWsPath);
    for (String fileWsPath : fileWsPaths) {
      if (isJsonFile(fileWsPath)) {
        linesPerFile.put(nameOf(fileWsPath), Integer.toString(countLines(fileWsPath)));
      }
    }

    return linesPerFile;
  }
}
