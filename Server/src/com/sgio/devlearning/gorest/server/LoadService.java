/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sgio.devlearning.gorest.server;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sgio.devlearning.gorest.helpers.BadResponse;
import com.sgio.devlearning.gorest.helpers.Metadata;
import com.sgio.devlearning.gorest.helpers.ProgressListener;
import com.sgio.devlearning.gorest.helpers.RateLimit;
import com.sgio.devlearning.gorest.models.*;

import com.sgio.devlearning.gorest.helpers.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author sgioh
 */
public final class LoadService {  
    private static final Collection<Person> users = new ArrayList();
    private static final Collection<Post> posts = new ArrayList();
    private static final Collection<Comment> comments = new ArrayList();
    private static final Collection<Album> albums = new ArrayList();
    private static final Collection<Photo> photos = new ArrayList();
       
    private static final ReentrantLock syncRoot = new ReentrantLock();
    private static final List<ProgressListener> progressListeners = new ArrayList();
    
    private static LoadService instance;
    private static String token;
    private static String url;
    
    private static Boolean operationCanceled;
    
    private LoadService() {
        operationCanceled = false;
    }
    
    public static LoadService getInstance(String baererToken, String baseUrl) {
        if (instance == null) {
            instance = new LoadService();
            token = baererToken;
            url = baseUrl;  
        }
        return instance;
    }
    
    public static LoadService getInstance() {
        return getInstance(token, url);
    }

    public Collection<Person> getUsers() {
        List<Person> copy = new ArrayList();
        try {
            syncRoot.lock();
            return users;
        } finally {
            syncRoot.unlock();
        }
        //return copy;
    }

    public Collection<Post> getPosts() {
        List<Post> copy = new ArrayList();
        try {
            syncRoot.lock();
            Collections.copy(copy, new ArrayList(posts));
        } finally {
            syncRoot.unlock();
        }
        return copy;
    }

    public Collection<Comment> getComments() {
        List<Comment> copy = new ArrayList();
        try {
            syncRoot.lock();
            Collections.copy(copy, new ArrayList(comments));
        } finally {
            syncRoot.unlock();
        }
        return copy;
    }

    public Collection<Album> getAlbums() {
        List<Album> copy = new ArrayList();
        try {
            syncRoot.lock();
            Collections.copy(copy, new ArrayList(albums));
        } finally {
            syncRoot.unlock();
        }
        return copy;
    }

    public Collection<Photo> getPhotos() {
        List<Photo> copy = new ArrayList();
        try {
            syncRoot.lock();
            Collections.copy(copy, new ArrayList(photos));
        } finally {
            syncRoot.unlock();
        }
        return copy;
    }
    
    public void addProgressListener(ProgressListener listener) {
        try {
            syncRoot.lock();
            progressListeners.add(listener);
        } finally {
            syncRoot.unlock();
        }
    }
    
    public void clearCache() {
        try {
            syncRoot.lock();
            
            users.clear();
            posts.clear();
            comments.clear();
            albums.clear();
            photos.clear();
        } finally {
            syncRoot.unlock();
        }
    }
    
    public void reloadDataFromSource() {
        this.clearCache();
        this.loadDataFromSource();
    }
    
    public void cancelLoad(String reason) {
        try {
            syncRoot.lock();
            
            if (!operationCanceled){
                operationCanceled = true;
                progressListeners.stream().forEach((listener) -> {
                    listener.operationFailed(reason);
                });
            }
        } finally {
            syncRoot.unlock();
        }
    }
    
    public void loadDataFromSource() {
        List<Person> tempUsers = new ArrayList();
        List<Post> tempPosts = new ArrayList();
        List<Comment> tempComments = new ArrayList();
        List<Album> tempAlbums = new ArrayList();
        List<Photo> tempPhotos = new ArrayList();
        
        CompletableFuture<List<Person>> loadUsers = this.loadUsers().thenApply(r -> { tempUsers.addAll(r); return r; });
        
        CompletableFuture<List<Post>> loadPosts = loadUsers.thenCompose(u -> this.loadPostsByUsers((List<Person>) u));
                                        
        loadPosts.thenAccept(r -> tempPosts.addAll(r));
        
        CompletableFuture<List<Album>> loadAlbums = loadUsers.thenCompose(u -> this.loadAlbumsByUsers((List<Person>) u));
        
        loadAlbums.thenAccept(r -> tempAlbums.addAll(r));
        
        CompletableFuture<List<Comment>> loadComments = loadPosts.thenCompose(p -> this.loadCommentsByPosts((List<Post>) p));
        
        loadComments.thenAccept(r -> tempComments.addAll(r));
        
        CompletableFuture<List<Photo>> loadPhotos = loadAlbums.thenCompose(a -> this.loadPhotosByAlbums((List<Album>) a));
        
        loadPhotos.thenAccept(r -> tempPhotos.addAll(r));
        
        CompletableFuture.allOf(loadUsers, loadPosts, loadAlbums, loadComments, loadPhotos)
                        .thenAccept(r -> 
                        { 
                            users.addAll(tempUsers);
                            posts.addAll(tempPosts);
                            albums.addAll(tempAlbums);
                            comments.addAll(tempComments);
                            photos.addAll(tempPhotos);
                            
                            System.out.println("Completed");
                        });
    }
    
    private CompletableFuture<List<Photo>> loadPhotosByAlbums(List<Album> albums) {
        return CompletableFuture.supplyAsync(()-> {
            
            System.out.println("Descargando fotos para " + albums.size() + " albumes encontrados.");

            final List<Photo> buffer = new ArrayList();
            try {
                for(int i = 0; i < albums.size(); i++) {
                    if (!operationCanceled) {
                        final int id = albums.get(i).getId();
                        System.out.println("Descargando fotos para album con id: " + id);
                        final Response<Photo> r = httpGet("photos?album_id=" + id, new TypeToken<Response<Photo>>(){}.getType(), 5);

                        RateLimit rate = r.getMetadata().getRateLimit();
                        if (rate.getRemaining() < users.size()) {
                            Thread.sleep(500);
                        }
                        
                        final Collection<Photo> filtered = r.getData()
                                                           .stream()
                                                           .filter(p -> p.getAlbumId() == id)
                                                           .collect(Collectors.toList());
                        buffer.addAll(filtered);
                        //this.notifyProgress( ((i+1)*20)/albums.size() );
                    } else {
                       buffer.clear();
                       break;
                    }
                }
            } catch (Exception ex) {
                this.cancelLoad(ex.toString());
            }
            this.notifyProgress(20);
            return buffer;
        });
    }
    
    private CompletableFuture<List<Album>> loadAlbumsByUsers(List<Person> users) {
        return CompletableFuture.supplyAsync(()-> {
            
            System.out.println("Descargando albumes para " + users.size() + " usuarios encontrados.");
            
            final List<Album> buffer = new ArrayList();
            try {
                for(int i = 0; i < users.size(); i++) {
                    if (!operationCanceled) {
                        final int id = users.get(i).getId();
                        System.out.println("Descargando album para usuario con id: " + id);
                        final Response<Album> r = httpGet("albums?user_id=" + id, new TypeToken<Response<Album>>(){}.getType(), 5);

                        RateLimit rate = r.getMetadata().getRateLimit();
                        if (rate.getRemaining() < users.size()) {
                            Thread.sleep(800);
                        }
                        
                        final Collection<Album> filtered = r.getData()
                                                           .stream()
                                                           .filter(p -> p.getUserId() == id)
                                                           .collect(Collectors.toList());
                        buffer.addAll(filtered);
                        //this.notifyProgress(((i+1)*20)/users.size());
                    } else {
                       buffer.clear();
                       break;
                    }
                }
            } catch (Exception ex) {
                this.cancelLoad(ex.toString());
            }
            this.notifyProgress(20);
            return buffer;
        });
    }
    
    private CompletableFuture<List<Comment>> loadCommentsByPosts(List<Post> posts) {
        return CompletableFuture.supplyAsync(()-> {
            
            System.out.println("Descargando comentarios para " + posts.size() + " posts encontrados.");
            
            final List<Comment> buffer = new ArrayList();
            try {
                for(int i = 0; i < posts.size(); i++) {
                    if (!operationCanceled) {
                        final int id = posts.get(i).getId();
                        System.out.println("Descargando comentarios para post con id: " + id);
                        final Response<Comment> r = httpGet("comments?post_id=" + id, new TypeToken<Response<Comment>>(){}.getType(), 5);

                        RateLimit rate = r.getMetadata().getRateLimit();
                        if (rate.getRemaining() < posts.size()) {
                            Thread.sleep(800);
                        }
                        
                        final Collection<Comment> filtered = r.getData()
                                                           .stream()
                                                           .filter(p -> p.getPostId() == id)
                                                           .collect(Collectors.toList());
                        buffer.addAll(filtered);
                        //this.notifyProgress(20 * (((i+1)*20)/posts.size()));
                    } else {
                       buffer.clear();
                       break;
                    }
                }
            } catch (Exception ex) {
                this.cancelLoad(ex.toString());
            }
            this.notifyProgress(20);
            return buffer;
        });
    }
    
    private CompletableFuture<List<Post>> loadPostsByUsers(List<Person> users) {
        return CompletableFuture.supplyAsync(()-> {
            
            System.out.println("Descargando posts para " + users.size() + " usuarios encontrados.");
            
            final List<Post> buffer = new ArrayList();
            try {
                for(int i = 0; i < users.size(); i++) {
                    if (!operationCanceled) {
                        final int id = users.get(i).getId();
                        System.out.println("Descargando posts para usuario con id: " + id);
                        final Response<Post> r = httpGet("posts?user_id=" + id, new TypeToken<Response<Post>>(){}.getType(), 5);
                        
                        RateLimit rate = r.getMetadata().getRateLimit();
                        
                        if (rate.getRemaining() < users.size()) {
                            Thread.sleep(800);
                        }
                        
                        final Collection<Post> filtered = r.getData()
                                                           .stream()
                                                           .filter(p -> p.getUserId() == id)
                                                           .collect(Collectors.toList());
                        buffer.addAll(filtered);
                        //this.notifyProgress(((i+1)*20)/users.size());
                    } else {
                       buffer.clear();
                       break;
                    }
                }
            } catch (Exception ex) {
                this.cancelLoad(ex.getMessage());
            }
            this.notifyProgress(20);
            return buffer;
        });
    }
    
    private CompletableFuture<List<Person>> loadUsers() {
        return CompletableFuture.supplyAsync(()-> {
            
            System.out.println("Descargando usuarios.");
            
            Response<Person> r = Response.empty(Person.class);
            
            try {
                r = httpGet("users", new TypeToken<Response<Person>>(){}.getType(), 5);
            } catch (Exception ex) { this.cancelLoad(ex.getMessage()); }
            
            return r.getData();
        }).thenApply(r -> {
                if (!operationCanceled) {
                    this.notifyProgress(20);
                }
                return new ArrayList(r);
            }
        );
    }
    
    private void notifyProgress(float deltaProgress) {
        try {
            syncRoot.lock();
            progressListeners.stream().forEach((listener) -> {
                listener.progressChanged(deltaProgress);
            });
        } finally {
            syncRoot.unlock();
        }
    }
    
    private <T> Response<T> httpGet(String query, Type type, int maxAttemps) throws IOException { 
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(url+query);
            
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "Bearer " + token);
            
            HttpResponse result = httpClient.execute(request);
            
            String json = EntityUtils.toString(result.getEntity(), "UTF-8");  
            
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();

            try {
                return gson.fromJson(json, type);
            } catch(JsonSyntaxException ex) {
                BadResponse badResponse = gson.fromJson(json, new TypeToken<BadResponse>(){}.getType());

                Metadata metadata = badResponse.getMetadata();
                if (metadata.getSuccess() || metadata.getCode() != 429) {
                    throw new IOException();
                }
                
                System.out.println(badResponse.getMetadata().getMessage());

                try {
                    Thread.sleep(6000);
                } catch (InterruptedException ex1) {
                    System.out.println(ex.getMessage());
                }
                
                if (maxAttemps > 0) {
                    maxAttemps--;
                    
                    System.out.println("Reintentando descarga.");
                    
                    return httpGet(query, type, maxAttemps);
                } else {
                    throw new IOException("Se excedió el número de intentos.");
                }
            }
        }
    }
}
