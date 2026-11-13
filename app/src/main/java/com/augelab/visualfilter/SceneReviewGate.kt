package com.augelab.visualfilter

/** No pixel-motion heuristic. Window, media geometry and actual scroll events invalidate work.
 * Pending remains visible, never a classifier BLOCK label.
 */
enum class ReviewState { CHECKING, BLOCK, SAFE }
data class TrackedMedia(val identity:String?,val bounds:PixelRect)
class SceneReviewGate {
 private var tracked:List<TrackedMedia> = emptyList()
 var retainedBlockDuringMove=false;private set
 var generation=0L;private set
 var windowId:Int?=null;private set
 var states:List<ReviewState> = emptyList();private set
 var scores:List<Float?> = emptyList();private set
 var blockScores:List<Float?> = emptyList();private set
 var moments:List<JourneyMoment?> = emptyList();private set
 private var layout=""
 fun updateWindow(id:Int,key:String,regions:Int):Boolean = updateTrackedWindow(id,key,List(regions){TrackedMedia(null,PixelRect(0,0,1,1))})
 fun updateTrackedWindow(id:Int,key:String,media:List<TrackedMedia>):Boolean {
  require(media.isNotEmpty())
  if(windowId==id && layout==key && tracked==media)return false
  retainedBlockDuringMove=false
  val nextScores=mutableListOf<Float?>()
  val nextBlockScores=mutableListOf<Float?>()
  val nextMoments=mutableListOf<JourneyMoment?>()
  val next=media.map{m->
   val old=if(windowId==id && m.identity!=null)tracked.indexOfFirst{it.identity==m.identity}else -1
   val previous=tracked.getOrNull(old)
   val movesSameImage=previous!=null && previous.bounds!=m.bounds && previous.bounds.width==m.bounds.width && previous.bounds.clipped(m.bounds)!=null
   val sameImage=previous!=null && (previous.bounds==m.bounds || movesSameImage)
   if(sameImage && states.getOrNull(old)==ReviewState.BLOCK){
    nextScores.add(scores.getOrNull(old));nextBlockScores.add(blockScores.getOrNull(old));nextMoments.add(moments.getOrNull(old))
    retainedBlockDuringMove=retainedBlockDuringMove || movesSameImage;ReviewState.BLOCK
   }else{nextScores.add(null);nextBlockScores.add(null);nextMoments.add(null);ReviewState.CHECKING}
  }
  windowId=id;layout=key;tracked=media;states=next;scores=nextScores;blockScores=nextBlockScores;moments=nextMoments;generation++;return true
 }
 fun scrolled(preserveBlocks:Boolean=false){if(windowId!=null){generation++;states=states.map{if(preserveBlocks && it==ReviewState.BLOCK)it else ReviewState.CHECKING};scores=scores.mapIndexed{i,v->if(states[i]==ReviewState.BLOCK)v else null};blockScores=blockScores.mapIndexed{i,v->if(states[i]==ReviewState.BLOCK)v else null};moments=moments.mapIndexed{i,v->if(states[i]==ReviewState.BLOCK)v else null}}}
 fun markCounted(token:Long,index:Int,moment:JourneyMoment):Boolean{
  if(token!=generation || states.getOrNull(index)!=ReviewState.BLOCK || moments.getOrNull(index)!=null)return false
  moments=moments.toMutableList().apply{this[index]=moment};return true
 }
 fun accept(token:Long,index:Int,block:Boolean,score:Float?=null):Boolean {
  if(windowId==null || token!=generation || index !in states.indices)return false
  val validScore=score?.takeIf{it.isFinite() && it in 0f..1f}
  scores=scores.toMutableList().apply{this[index]=validScore}
  if(block && states[index]!=ReviewState.BLOCK)blockScores=blockScores.toMutableList().apply{this[index]=validScore}
  // A video may alternate between safe and unsafe frames. BLOCK belongs to the
  // tracked media, not just its current frame, until the media leaves or changes.
  states=states.toMutableList().apply{this[index]=if(block || states[index]==ReviewState.BLOCK)ReviewState.BLOCK else ReviewState.SAFE};return true
 }
 fun leave(){if(windowId!=null){generation++;windowId=null;layout="";states=emptyList();scores=emptyList();blockScores=emptyList();moments=emptyList();tracked=emptyList();retainedBlockDuringMove=false}}
}

/** Exact OR short circuit: an enabled region already establishes the combined block decision. */
