/*
    Copyright 2012 The Whitehole team

    This file is part of Whitehole.

    Whitehole is free software: you can redistribute it and/or modify it under
    the terms of the GNU General Public License as published by the Free
    Software Foundation, either version 3 of the License, or (at your option)
    any later version.

    Whitehole is distributed in the hope that it will be useful, but WITHOUT ANY 
    WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
    FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along 
    with Whitehole. If not, see http://www.gnu.org/licenses/.
*/

package whitehole.rendering;

import java.util.*;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLException;
import whitehole.smg.*;
import whitehole.vectors.*;

public class RendererCache 
{
    public static void initialize()
    {
        cache = new HashMap<>();
        refContext = null;
        contextCount = 0;
    }
    
    public static GLRenderer getObjectRenderer(GLRenderer.RenderInfo info, LevelObject obj)
    {
        String modelname = obj.name;
        modelname = ObjectModelSubstitutor.substituteModelName(obj, modelname);
        
        String key = "object_" + obj.name;
        key = ObjectModelSubstitutor.substituteObjectKey(obj, key);
        
        if (cache.containsKey(key))
        {
            CacheEntry entry = cache.get(key);
            entry.refCount++;
            return entry.renderer;
        }
        
        CacheEntry entry = new CacheEntry();
        entry.refCount = 1;
        
        entry.renderer = ObjectModelSubstitutor.substituteRenderer(obj, info);
        
        // if no renderer substitution happened, load the default renderer
        if (entry.renderer == null)
        {
            try
            {
                entry.renderer = new BmdRenderer(info, modelname);
            }
            catch (GLException ex)
            {
                entry.renderer = null;
            }
        }
        
        // if everything else failed, load the failsafe colorcube renderer
        if (entry.renderer == null)
            entry.renderer = new ColorCubeRenderer(100f, new Color4(1f, 1f, 1f, 1f), new Color4(0f, 0f, 1f, 1f), true);
        
        cache.put(key, entry);
        return entry.renderer;
    }
    
    public static void closeObjectRenderer(GLRenderer.RenderInfo info, LevelObject obj)
    {
        String key = "object_" + obj.name;
        key = ObjectModelSubstitutor.substituteObjectKey(obj, key);
        if (!cache.containsKey(key)) return;
        
        CacheEntry entry = cache.get(key);
        entry.refCount--;
        if (entry.refCount > 0) return;
        
        entry.renderer.close(info);
        
        cache.remove(key);
    }
    
    public static void setRefContext(GLContext ctx)
    {
        if (refContext == null) refContext = ctx;
        contextCount++;
    }
    
    public static void clearRefContext()
    {
        contextCount--;
        if (contextCount < 1) refContext = null;
    }
    
    
    public static class CacheEntry
    {
        public GLRenderer renderer;
        public int refCount;
    }
    
    public static HashMap<String, CacheEntry> cache;
    public static GLContext refContext;
    public static int contextCount;
}
